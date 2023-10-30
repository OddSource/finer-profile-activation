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

import java.util.Locale;

import org.apache.maven.model.ActivationProperty;

interface StringContainsHelperMixin
{
    default boolean match(final String subject, final ActivationProperty property)
    {
        return this.match(subject, property, false);
    }

    default boolean match(final String subject, final ActivationProperty property, final boolean lowercaseIt)
    {
        if (subject == null || subject.isEmpty())
        {
            return false;
        }
        return lowercaseIt ?
               subject.toLowerCase(Locale.US).contains(property.getValue()) :
               subject.contains(property.getValue());
    }
}
