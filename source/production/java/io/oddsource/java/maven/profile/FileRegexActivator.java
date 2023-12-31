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
 * Determines if the file named by the property name exists and its contents match the regex from the property value.
 *
 * @since 1.0.0
 */
@Named("propertyFileRegexActivator")
@Singleton
public class FileRegexActivator extends BaseFileContentsActivator implements RegexHelperMixin
{
    private static final String BRACKET_NAME = "FILE.REGEX";

    /**
     * Construct a FileRegexActivator.
     *
     * @param logger A Maven logger, auto-injected by Maven
     */
    @Inject
    public FileRegexActivator(final Logger logger)
    {
        super(logger);
    }

    @Override
    public String getSupportedActivatorBracketName()
    {
        return FileRegexActivator.BRACKET_NAME;
    }

    @Override
    protected boolean contentsMatch(
        final String contents,
        final ActivationProperty property,
        final ModelProblemCollector problems)
    {
        return this.match(contents, property, problems);
    }
}
