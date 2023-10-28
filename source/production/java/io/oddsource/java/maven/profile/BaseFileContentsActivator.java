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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.codehaus.plexus.logging.Logger;

abstract class BaseFileContentsActivator extends BaseFinerActivator
{
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final String PRELUDE = "The file named by the property 'name' ('";

    private final Logger logger;

    /**
     * Construct a BaseFileContentsActivator.
     *
     * @param logger A Maven logger, auto-injected by Maven
     */
    protected BaseFileContentsActivator(final Logger logger)
    {
        this.logger = logger;
    }

    @Override
    @SuppressWarnings("checkstyle:ReturnCount")
    public boolean isActive(
        final String unparsedFileName,
        final ActivationProperty property,
        final Profile profile,
        final ProfileActivationContext context,
        final ModelProblemCollector problems
    )
    {
        final Utilities.CharsetAndRemainder helper = new Utilities.CharsetAndRemainder(
            unparsedFileName,
            BaseFileContentsActivator.DEFAULT_CHARSET,
            property,
            problems
        );
        final Charset charset = helper.getCharset();
        final String fileName = helper.getRemainder();
        if(charset == null || fileName == null)
        {
            this.logger.debug("BaseFileContentsActivator: charset == null || filename == null");
            return false;
        }

        final File file = this.getAbsoluteFile(fileName, property, problems);
        if(file == null || !file.exists())
        {
            this.logger.debug("BaseFileContentsActivator(" + fileName + "): file == null || !file.exists()");
            return false;
        }

        final String contents;
        try
        {
            contents = new String(Files.readAllBytes(file.toPath()), charset);
        }
        catch(final IOException e)
        {
            return Utilities.problem(problems, property,
                BaseFileContentsActivator.PRELUDE + fileName + "') exists but could not be read."
            );
        }

        if(this.logger.isDebugEnabled())
        {
            this.logger.debug("BaseFileContentsActivator: contents of file " + fileName + " = " + contents);
        }

        return !contents.isEmpty() && this.contentsMatch(contents, property, problems);
    }

    private File getAbsoluteFile(
        final String fileName,
        final ActivationProperty property,
        final ModelProblemCollector problems
    )
    {
        try
        {
            return new File(fileName).getAbsoluteFile();
        }
        catch(final SecurityException e)
        {
            Utilities.problem(problems, property,
                BaseFileContentsActivator.PRELUDE + fileName + "') is not accessible."
            );
            return null;
        }
    }

    /**
     * Called when the file has been located, exists, and has been successfully read, so that the concrete
     * activator can validate the contents.
     *
     * @param contents The file contents
     * @param property The property object
     * @param problems A collector of problems
     * @return whether the file contents match.
     */
    protected abstract boolean contentsMatch(
        String contents,
        ActivationProperty property,
        ModelProblemCollector problems
    );
}
