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

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.codehaus.plexus.logging.Logger;
import org.easymock.Capture;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.oddsource.java.maven.profile.SubprocessExitCodeActivator;
import io.oddsource.java.maven.profile.SubprocessOutputActivator;
import io.oddsource.java.maven.profile.SubprocessRegexActivator;

public class TestSubprocessActivators extends EasyMockSupport
{
    /**
     * Mocking.
     */
    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    private Logger logger;

    @Mock
    private ModelProblemCollector problems;

    @Before
    public void setUp()
    {
        expect(this.logger.isDebugEnabled()).andReturn(true).anyTimes();
        this.logger.debug(anyString());
        expectLastCall().anyTimes();
        this.logger.info(anyString());
        expectLastCall().anyTimes();
    }

    @Test
    public void testExitCodeActivatorInvalidNumber()
    {
        final Capture<ModelProblemCollectorRequest> capture = newCapture();
        this.problems.add(capture(capture));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("Not A Number!");

        final ExitCode activator = new ExitCode(this.logger);
        final boolean result = activator.processResultMatches(0, "hello", property, this.problems);
        assertFalse(result);
        assertTrue(capture.hasCaptured());

        final ModelProblemCollectorRequest request = capture.getValue();
        assertNotNull(request);
        assertEquals(ModelProblem.Severity.ERROR, request.getSeverity());
        assertEquals(ModelProblem.Version.BASE, request.getVersion());
        assertEquals(
            "The property 'value' ('Not A Number!') could not be converted to a long.",
            request.getMessage()
        );

        this.verifyAll();
    }

    @Test
    public void testExitCodeActivatorCodesDoNotMatch()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("0");

        final ExitCode activator = new ExitCode(this.logger);
        final boolean result = activator.processResultMatches(5, "hello", property, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testExitCodeActivatorCodesMatch()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("10");

        final ExitCode activator = new ExitCode(this.logger);
        final boolean result = activator.processResultMatches(10, "hello", property, this.problems);
        assertTrue(result);

        this.verifyAll();
    }

    @Test
    public void testOutputActivatorContentsEmpty()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final Output activator = new Output(this.logger);
        final boolean result = activator.processResultMatches(0, "", property, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testOutputActivatorContentsDoNotMatch()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final Output activator = new Output(this.logger);
        final boolean result = activator.processResultMatches(
            0,
            "No lowercase HELLO in here",
            property,
            this.problems
        );
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testOutputActivatorContentsMatch()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("hello");

        final Output activator = new Output(this.logger);
        final boolean result = activator.processResultMatches(
            0,
            "Yes lowercase hello in here",
            property,
            this.problems
        );
        assertTrue(result);

        this.verifyAll();
    }

    @Test
    public void testRegexActivatorInvalidPatternWithNonEmptyOutputReportsProblem()
    {
        final Capture<ModelProblemCollectorRequest> capture = newCapture();
        this.problems.add(capture(capture));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(()([/this should confuse it");

        final Regex activator = new Regex(this.logger);
        final boolean result = activator.processResultMatches(0, "hello", property, this.problems);
        assertFalse(result);
        assertTrue(capture.hasCaptured());

        final ModelProblemCollectorRequest request = capture.getValue();
        assertNotNull(request);

        this.verifyAll();
    }

    @Test
    public void testRegexActivatorInvalidPatternWithEmptyOutputStillReportsProblem()
    {
        final Capture<ModelProblemCollectorRequest> capture = newCapture();
        this.problems.add(capture(capture));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(()([/this should confuse it");

        final Regex activator = new Regex(this.logger);
        final boolean result = activator.processResultMatches(0, "", property, this.problems);
        assertFalse(result);
        assertTrue(capture.hasCaptured());

        final ModelProblemCollectorRequest request = capture.getValue();
        assertNotNull(request);

        this.verifyAll();
    }

    @Test
    public void testRegexActivatorEmptyOutput()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final Regex activator = new Regex(this.logger);
        final boolean result = activator.processResultMatches(0, "", property, this.problems);
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testRegexActivatorNoMatch()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final Regex activator = new Regex(this.logger);
        final boolean result = activator.processResultMatches(
            0,
            "somewhere in this output\nwe do not say\nwhat it is looking for",
            property,
            this.problems
        );
        assertFalse(result);

        this.verifyAll();
    }

    @Test
    public void testRegexActivatorWithMatch()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();
        property.setValue("(hello|goodbye)");

        final Regex activator = new Regex(this.logger);
        final boolean result = activator.processResultMatches(
            0,
            "somewhere in this output\nwe say hello\nand that's what it is looking for",
            property,
            this.problems
        );
        assertTrue(result);

        this.verifyAll();
    }

    private static final class ExitCode extends SubprocessExitCodeActivator
    {
        private ExitCode(final Logger logger)
        {
            super(logger);
        }

        @Override
        protected boolean processResultMatches(
            final int e,
            final String o,
            final ActivationProperty p,
            final ModelProblemCollector c
        )
        {
            return super.processResultMatches(e, o, p, c);
        }
    }

    private static final class Output extends SubprocessOutputActivator
    {
        private Output(final Logger logger)
        {
            super(logger);
        }

        @Override
        protected boolean processResultMatches(
            final int e,
            final String o,
            final ActivationProperty p,
            final ModelProblemCollector c
        )
        {
            return super.processResultMatches(e, o, p, c);
        }
    }

    private static final class Regex extends SubprocessRegexActivator
    {
        private Regex(final Logger logger)
        {
            super(logger);
        }

        @Override
        protected boolean processResultMatches(
            final int e,
            final String o,
            final ActivationProperty p,
            final ModelProblemCollector c
        )
        {
            return super.processResultMatches(e, o, p, c);
        }
    }
}
