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

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.building.ModelProblemCollector;

interface RegexHelperMixin
{
    default boolean match(
        final String subject,
        final ActivationProperty property,
        final ModelProblemCollector problems
    )
    {
        final Pattern pattern = Utilities.getPattern(property, problems, Pattern.MULTILINE);
        return subject != null && !subject.isEmpty() && pattern != null && pattern.matcher(subject).find();
    }
}
