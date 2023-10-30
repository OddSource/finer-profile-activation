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

import java.util.Collections;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.easymock.Capture;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Rule;
import org.junit.Test;

import io.oddsource.java.maven.profile.PropertyContainsActivator;
import io.oddsource.java.maven.profile.PropertyRegexActivator;

public class TestPropertyActivators extends EasyMockSupport
{
    /**
     * Mocking.
     */
    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    private ProfileActivationContext context;

    @Mock
    private ModelProblemCollector problems;

    @Test
    public void testPropertyContainsButNoValue()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(Collections.emptyMap());

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsButFirstValueEmpty()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("foo-prop", ""));
        expect(this.context.getSystemProperties()).andReturn(Collections.emptyMap());

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsButSecondValueEmpty()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(Collections.singletonMap("foo-prop", ""));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsButBothValuesEmpty()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("foo-prop", ""));
        expect(this.context.getSystemProperties()).andReturn(Collections.singletonMap("foo-prop", ""));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsFirstValueNullSecondDoesNotContain()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(Collections.singletonMap("foo-prop", "goodbye"));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsFirstValueNullSecondDoesContain()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(
            Collections.singletonMap("foo-prop", "Well HELLO, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertTrue(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsFirstValueEmptySecondDoesNotContain()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("foo-prop", ""));
        expect(this.context.getSystemProperties()).andReturn(Collections.singletonMap("foo-prop", "goodbye"));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsFirstValueEmptySecondDoesContain()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("foo-prop", ""));
        expect(this.context.getSystemProperties()).andReturn(
            Collections.singletonMap("foo-prop", "Well HELLO, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertTrue(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsFirstValueDoesNotContain()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("bar-baz", "hello"));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("goodbye");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("bar-baz", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyContainsFirstValueDoesContain()
    {
        expect(this.context.getUserProperties()).andReturn(
            Collections.singletonMap("bar-baz", "Well GOODBYE, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("goodbye");

        final PropertyContainsActivator activator = new PropertyContainsActivator();
        final boolean result = activator.isActive("bar-baz", property, null, this.context, this.problems);
        assertTrue(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexInvalidPatternAndNoValue()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(Collections.emptyMap());

        final Capture<ModelProblemCollectorRequest> capture = newCapture();
        this.problems.add(capture(capture));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(()([/this should confuse it");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);
        assertTrue(capture.hasCaptured());

        final ModelProblemCollectorRequest request = capture.getValue();
        assertNotNull(request);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexButNoValue()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(Collections.emptyMap());

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexButFirstValueEmpty()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("foo-prop", ""));
        expect(this.context.getSystemProperties()).andReturn(Collections.emptyMap());

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexButSecondValueEmpty()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(Collections.singletonMap("foo-prop", ""));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexButBothValuesEmpty()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("foo-prop", ""));
        expect(this.context.getSystemProperties()).andReturn(Collections.singletonMap("foo-prop", ""));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexFirstValueNullSecondDoesNotMatch()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(
            Collections.singletonMap("foo-prop", "Well GOODBYE, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexFirstValueNullSecondDoesMatch()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.emptyMap());
        expect(this.context.getSystemProperties()).andReturn(
            Collections.singletonMap("foo-prop", "Well goodbye, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertTrue(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexFirstValueEmptySecondDoesNotMatch()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("foo-prop", ""));
        expect(this.context.getSystemProperties()).andReturn(
            Collections.singletonMap("foo-prop", "Well HELLO, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexFirstValueEmptySecondDoesMatch()
    {
        expect(this.context.getUserProperties()).andReturn(Collections.singletonMap("foo-prop", ""));
        expect(this.context.getSystemProperties()).andReturn(
            Collections.singletonMap("foo-prop", "Well hello, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("foo-prop", property, null, this.context, this.problems);
        assertTrue(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexFirstValueDoesNotMatch()
    {
        expect(this.context.getUserProperties()).andReturn(
            Collections.singletonMap("bar-baz", "Well GOODBYE, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("bar-baz", property, null, this.context, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testPropertyRegexFirstValueDoesMatch()
    {
        expect(this.context.getUserProperties()).andReturn(
            Collections.singletonMap("bar-baz", "Well goodbye, world!")
        );

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final PropertyRegexActivator activator = new PropertyRegexActivator();
        final boolean result = activator.isActive("bar-baz", property, null, this.context, this.problems);
        assertTrue(result);

        this.verifyAll();
    }
}
