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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.easymock.Capture;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Rule;
import org.junit.Test;

import io.oddsource.java.maven.profile.Utilities;

public class TestUtilities extends EasyMockSupport
{
    /**
     * Mocking.
     */
    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    private ModelProblemCollector collector;

    @Test
    public void testGetPattern()
    {
        replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("^hello$");

        final Pattern pattern = Utilities.getPattern(property, this.collector);

        verifyAll();

        assertNotNull(pattern);
        assertTrue(pattern.matcher("hello").matches());
        assertFalse(pattern.matcher("HELLO").matches());
        assertFalse(pattern.matcher("hello-world").matches());
        assertFalse(pattern.matcher("goodbye").matches());
    }

    @Test
    public void testGetPatternWithFlags()
    {
        replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("^goodbye$");

        final Pattern pattern = Utilities.getPattern(property, this.collector, Pattern.CASE_INSENSITIVE);

        verifyAll();

        assertNotNull(pattern);
        assertTrue(pattern.matcher("goodbye").matches());
        assertTrue(pattern.matcher("GOODBYE").matches());
        assertFalse(pattern.matcher("goodbye-world").matches());
        assertFalse(pattern.matcher("hello").matches());
    }

    @Test
    public void testGetPatternWithProblems()
    {
        final Capture<ModelProblemCollectorRequest> capture = newCapture();
        this.collector.add(capture(capture));
        replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(()([/this should confuse it");

        final Pattern pattern = Utilities.getPattern(property, this.collector);

        verifyAll();

        assertNull(pattern);
        assertTrue(capture.hasCaptured());

        final ModelProblemCollectorRequest request = capture.getValue();
        assertNotNull(request);
        assertEquals(ModelProblem.Severity.ERROR, request.getSeverity());
        assertEquals(ModelProblem.Version.BASE, request.getVersion());
        assertEquals(
            "The property 'value' ('(()([/this should confuse it') could not be compiled to a regular expression.",
            request.getMessage()
        );
    }

    @Test
    public void testTokenize()
    {
        replayAll();

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

        verifyAll();
    }

    @Test
    public void testCharsetAndRemainderNoCharset()
    {
        replayAll();

        final ActivationProperty property = new ActivationProperty();

        Utilities.CharsetAndRemainder helper = new Utilities.CharsetAndRemainder(
            "this is the subject",
            StandardCharsets.UTF_8,
            property,
            this.collector
        );

        assertEquals("this is the subject", helper.getRemainder());
        assertEquals(StandardCharsets.UTF_8, helper.getCharset());

        helper = new Utilities.CharsetAndRemainder(
            "this is another subject",
            StandardCharsets.ISO_8859_1,
            property,
            this.collector
        );

        verifyAll();

        assertEquals("this is another subject", helper.getRemainder());
        assertEquals(StandardCharsets.ISO_8859_1, helper.getCharset());
    }

    @Test
    public void testCharsetAndRemainderValidCharset()
    {
        replayAll();

        final ActivationProperty property = new ActivationProperty();

        final Utilities.CharsetAndRemainder helper = new Utilities.CharsetAndRemainder(
            "[ISO-8859-1]everything after this should be kept",
            StandardCharsets.UTF_8,
            property,
            this.collector
        );

        verifyAll();

        assertEquals("everything after this should be kept", helper.getRemainder());
        assertEquals(StandardCharsets.ISO_8859_1, helper.getCharset());
    }

    @Test
    public void testCharsetAndRemainderUnsupportedCharset()
    {
        final Capture<ModelProblemCollectorRequest> capture = newCapture();
        this.collector.add(capture(capture));
        replayAll();

        final ActivationProperty property = new ActivationProperty();

        final Utilities.CharsetAndRemainder helper = new Utilities.CharsetAndRemainder(
            "[RFC_2952_3]this should result in an error",
            StandardCharsets.UTF_8,
            property,
            this.collector
        );

        verifyAll();

        assertEquals("this should result in an error", helper.getRemainder());
        assertNull(helper.getCharset());
        assertTrue(capture.hasCaptured());

        final ModelProblemCollectorRequest request = capture.getValue();
        assertNotNull(request);
        assertEquals(ModelProblem.Severity.ERROR, request.getSeverity());
        assertEquals(ModelProblem.Version.BASE, request.getVersion());
        assertEquals(
            "The charset from the property 'name' ('[RFC_2952_3]this should result in an error') " +
            "is not supported.",
            request.getMessage()
        );
    }
}
