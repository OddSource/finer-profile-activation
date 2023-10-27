/*
 * Copyright © 2010-2023 OddSource Code (license@oddsource.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.oddsource.java.maven.profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.codehaus.plexus.logging.Logger;

abstract class BaseSubprocessActivator extends BaseFinerActivator
{
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final long TIMEOUT = 30;

    private static final String PRELUDE = "The command named by the property 'name' (`";

    private final Logger logger;

    /**
     * Construct a BaseSubprocessActivator.
     *
     * @param logger A Maven logger, auto-injected by Maven
     */
    protected BaseSubprocessActivator(final Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public boolean isActive(
        final String unparsedCommandString,
        final ActivationProperty property,
        final Profile profile,
        final ProfileActivationContext context,
        final ModelProblemCollector problems
    )
    {
        final Utilities.CharsetAndRemainder helper = new Utilities.CharsetAndRemainder(
            unparsedCommandString,
            BaseSubprocessActivator.DEFAULT_CHARSET,
            property,
            problems
        );
        final Charset charset = helper.getCharset();
        final String commandString = helper.getRemainder();
        if(charset == null || commandString == null)
        {
            this.logger.debug("BaseSubprocessActivator: charset == null || commandString == null");
            return false;
        }

        return this.executedAndTestProcess(Utilities.tokenize(commandString), charset, property, problems);
    }

    @SuppressWarnings("checkstyle.returncount")
    private boolean executedAndTestProcess(
        final List<String> command,
        final Charset charset,
        final ActivationProperty property,
        final ModelProblemCollector problems
    )
    {
        try
        {
            final Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            if(!process.waitFor(BaseSubprocessActivator.TIMEOUT, TimeUnit.SECONDS))
            {
                return Utilities.problem(problems, property,
                    BaseSubprocessActivator.PRELUDE + Arrays.toString(command.toArray()) +
                    "`) timed out after " + BaseSubprocessActivator.TIMEOUT + " seconds."
                );
            }
            try(
                InputStream input = process.getInputStream();
                InputStreamReader reader = new InputStreamReader(input, charset);
                BufferedReader buffer = new BufferedReader(reader)
            )
            {
                final String output = buffer.lines().collect(Collectors.joining("\n"));
                this.logOutput(command, output);
                return this.processResultMatches(process.exitValue(), output, property, problems);
            }
        }
        catch(final SecurityException e)
        {
            return Utilities.problem(problems, property,
                BaseSubprocessActivator.PRELUDE + Arrays.toString(command.toArray()) +
                "`) could not be executed because it violates security constraints."
            );
        }
        catch(final UnsupportedOperationException e)
        {
            return Utilities.problem(problems, property,
                BaseSubprocessActivator.PRELUDE + Arrays.toString(command.toArray()) +
                "`) could not be executed because the operating system does not support process creation."
            );
        }
        catch(final InterruptedException e)
        {
            return Utilities.problem(problems, property,
                BaseSubprocessActivator.PRELUDE + Arrays.toString(command.toArray()) + "`) was interrupted."
            );
        }
        catch(final IOException e)
        {
            return Utilities.problem(problems, property,
                BaseSubprocessActivator.PRELUDE + Arrays.toString(command.toArray()) +
                "`) could not be executed because an I/O error occurred: " + e
            );
        }
    }

    private void logOutput(final List<String> command, final String output)
    {
        if(this.logger.isDebugEnabled())
        {
            this.logger.debug(
                "BaseSubprocessActivator: output from command " + Arrays.toString(command.toArray()) +
                ": " + output
            );
        }
    }

    /**
     * Tests the process exit code and/or output in the manner specified by the concrete class.
     *
     * @param exitCode The process exit code
     * @param output The process standard and error output, combined
     * @param property The property
     * @param problems A collector of problems
     * @return whether the check succeeded.
     */
    protected abstract boolean processResultMatches(
        int exitCode,
        String output,
        ActivationProperty property,
        ModelProblemCollector problems
    );
}
