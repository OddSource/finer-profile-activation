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

import org.apache.maven.model.profile.ProfileActivationContext;

interface UserPropertiesHelperMixin
{
    default String getUserOrSystemProperty(final String name, final ProfileActivationContext context)
    {
        String sysValue = context.getUserProperties().get(name);
        if (sysValue == null || sysValue.isEmpty())
        {
            sysValue = context.getSystemProperties().get(name);
        }
        return sysValue;
    }
}
