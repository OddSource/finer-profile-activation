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

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;

/**
 * Converts the named property's value to lowercase and checks whether it contains the specified value.
 *
 * @since 1.0.0
 */
@Named("propertyContainsActivator")
@Singleton
public class PropertyContainsActivator extends BaseFinerActivator
    implements StringContainsHelperMixin, UserPropertiesHelperMixin
{
    private static final String BRACKET_NAME = "CONTAINS";

    /**
     * Construct a PropertyContainsActivator.
     */
    public PropertyContainsActivator()
    {
    }

    @Override
    public String getSupportedActivatorBracketName()
    {
        return PropertyContainsActivator.BRACKET_NAME;
    }

    @Override
    public boolean isActive(
        final String name,
        final ActivationProperty property,
        final Profile profile,
        final ProfileActivationContext context,
        final ModelProblemCollector problems
    )
    {
        return this.match(this.getUserOrSystemProperty(name, context), property, true);
    }
}
