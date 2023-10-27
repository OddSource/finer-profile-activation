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

package io.oddsource.java.tests.unit;

import static org.junit.Assert.*;

import org.junit.Test;

import io.oddsource.java.maven.profile.Utilities;

public class TestUtilities
{
    @Test
    public void testTokenize()
    {
        assertArrayEquals(
            new String[] {"lsb_release"},
            Utilities.tokenize("lsb_release").toArray()
        );
        assertArrayEquals(
            new String[] {"foo", "bar baz 'qux'", "lorem ipsum 'dalor' semet", "that", "is", "what", "I", "mean"},
            Utilities.tokenize(
                "foo \"bar baz 'qux'\" 'lorem ipsum \\'dalor\\' semet' that is what I mean"
            ).toArray()
        );
    }
}
