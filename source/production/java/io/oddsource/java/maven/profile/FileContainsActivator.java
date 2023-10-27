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
 * Determines if the file named by the property name exists and contains the property value within its contents.
 *
 * @since 1.0.0
 */
@Named("propertyFileContainsActivator")
@Singleton
public class FileContainsActivator extends BaseFileContentsActivator
{
    private static final String BRACKET_NAME = BRACKET_NAME_PREFIX + "FILE.CONTAINS";

    /**
     * Construct a FileContainsActivator.
     *
     * @param logger A Maven logger, auto-injected by Maven
     */
    @Inject
    public FileContainsActivator(final Logger logger)
    {
        super(logger);
    }

    @Override
    public String getSupportedActivatorBracketName()
    {
        return FileContainsActivator.BRACKET_NAME;
    }

    @Override
    protected boolean contentsMatch(
        final String contents,
        final ActivationProperty property,
        final ModelProblemCollector problems)
    {
        return contents.contains(property.getValue());
    }
}
