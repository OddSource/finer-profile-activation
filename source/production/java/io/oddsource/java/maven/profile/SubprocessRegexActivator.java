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

import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.building.ModelProblemCollector;
import org.codehaus.plexus.logging.Logger;

/**
 * Run the subprocess indicated by the property name and test that its output matches the regular expression
 * contained in the property value.
 *
 * @since 1.0.0
 */
@Named("propertySubprocessRegexActivator")
@Singleton
public class SubprocessRegexActivator extends BaseSubprocessActivator
{
    private static final String BRACKET_NAME = "SUBPROCESS.REGEX";

    /**
     * Construct a SubprocessRegexActivator.
     *
     * @param logger A Maven logger, auto-injected by Maven
     */
    @Inject
    public SubprocessRegexActivator(final Logger logger)
    {
        super(logger);
    }

    @Override
    public String getSupportedActivatorBracketName()
    {
        return SubprocessRegexActivator.BRACKET_NAME;
    }

    @Override
    protected boolean processResultMatches(
        final int exitCode,
        final String output,
        final ActivationProperty property,
        final ModelProblemCollector problems
    )
    {
        final Pattern pattern = Utilities.getPattern(property, problems, Pattern.MULTILINE);
        return !output.isEmpty() && pattern != null && pattern.matcher(output).find();
    }
}
