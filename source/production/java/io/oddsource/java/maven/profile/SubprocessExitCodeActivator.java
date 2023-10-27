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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.building.ModelProblemCollector;
import org.codehaus.plexus.logging.Logger;

/**
 * Run the subprocess indicated by the property name and tests that its exit code matches the numeric
 * value from the property value.
 *
 * @since 1.0.0
 */
@Named("propertySubprocessExitCodeActivator")
@Singleton
public class SubprocessExitCodeActivator extends BaseSubprocessActivator
{
    private static final String BRACKET_NAME = BRACKET_NAME_PREFIX + "SUBPROCESS.EXIT";

    /**
     * Construct a SubprocessExitCodeActivator.
     *
     * @param logger A Maven logger, auto-injected by Maven
     */
    @Inject
    public SubprocessExitCodeActivator(final Logger logger)
    {
        super(logger);
    }

    @Override
    public String getSupportedActivatorBracketName()
    {
        return SubprocessExitCodeActivator.BRACKET_NAME;
    }

    @Override
    protected boolean processResultMatches(
        final int exitCode,
        final String output,
        final ActivationProperty property,
        final ModelProblemCollector problems
    )
    {
        final long expectedExitCode;
        try
        {
            expectedExitCode = Long.parseLong(property.getValue());
        }
        catch(final NumberFormatException e)
        {
            return Utilities.problem(problems, property,
                "The property 'value' ('" + property.getValue() + "') could not be converted to a long"
            );
        }
        return exitCode == expectedExitCode;
    }
}
