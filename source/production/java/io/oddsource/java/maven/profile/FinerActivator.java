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

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;

/**
 * The interface to which all "finer" activators adhere.
 *
 * @since 1.0.0
 */
public interface FinerActivator
{
    /**
     * Get the String to match the name withing the brackets of the magic property name. For example,
     * if the profile activation {@code <property><name>} is {@code [FINER.ACTIVATOR.REGEX]os.name},
     * the activator whose activator bracket value matches "FINER.ACTIVATOR.REGEX" will be chosen.
     *
     * @return the supported activator bracket name.
     */
    String getSupportedActivatorBracketName();

    /**
     * Tests the activator to determine whether its result is truthy.
     *
     * @param name The name of the property with the brackets ({@code [FINER.ACTIVATOR.*]}) removed
     * @param property The property object
     * @param profile The profile
     * @param context The activation context
     * @param problems A collector of problems
     * @return whether the activator evaluation result is truthy.
     */
    boolean isActive(
        String name,
        ActivationProperty property,
        Profile profile,
        ProfileActivationContext context,
        ModelProblemCollector problems
    );
}
