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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblemCollectorRequest;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.codehaus.plexus.logging.Logger;
import org.easymock.Capture;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.oddsource.java.maven.profile.BaseFileContentsActivator;

public class TestBaseFileContentsActivator extends EasyMockSupport
{
    /**
     * Mocking.
     */
    @Rule
    public EasyMockRule rule = new EasyMockRule(this);

    @Mock
    private Logger logger;

    @Mock
    private ProfileActivationContext context;

    @Mock
    private ModelProblemCollector problems;

    private Base activator;

    private void setUpLogger()
    {
        expect(this.logger.isDebugEnabled()).andReturn(true).anyTimes();
        this.logger.debug(anyString());
        expectLastCall().anyTimes();
        this.logger.info(anyString());
        expectLastCall().anyTimes();
    }

    @Before
    public void setUp()
    {
        this.setUpLogger();
        replay(this.logger);

        this.activator = this.createMockBuilder(Base.class).
             addMockedMethod(
                 "contentsMatch",
                 String.class,
                 ActivationProperty.class,
                 ModelProblemCollector.class).
             withConstructor(Logger.class).
             withArgs(this.logger).
             mock();

        reset(this.logger);
        this.setUpLogger();
    }

    @Test
    public void testIsActiveInvalidCharset()
    {
        final Capture<ModelProblemCollectorRequest> capture = newCapture();
        this.problems.add(capture(capture));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();

        final boolean result = this.activator.isActive(
            "[FOO_5921_3]/foo/bar/baz",
            property,
            null,
            this.context,
            this.problems
        );

        this.verifyAll();

        assertFalse(result);
        assertTrue(capture.hasCaptured());
    }

    @Test
    public void testIsActiveFileDoesNotExist()
    {
        this.replayAll();

        final ActivationProperty property = new ActivationProperty();

        final boolean result = this.activator.isActive(
            "/foo/bar/baz",
            property,
            null,
            this.context,
            this.problems
        );

        this.verifyAll();

        assertFalse(result);
    }

    @Test
    public void testIsActiveFileExistsButException()
    {
        // warning ... this test may not pass on Windows as-is

        final Capture<ModelProblemCollectorRequest> capture = newCapture();
        this.problems.add(capture(capture));

        this.replayAll();

        final ActivationProperty property = new ActivationProperty();

        final boolean result = this.activator.isActive(
            "/",
            property,
            null,
            this.context,
            this.problems
        );

        this.verifyAll();

        assertFalse(result);
        assertTrue(capture.hasCaptured());

        final ModelProblemCollectorRequest request = capture.getValue();
        assertNotNull(request);
        assertEquals(ModelProblem.Severity.ERROR, request.getSeverity());
        assertEquals(ModelProblem.Version.BASE, request.getVersion());
        assertEquals(
            "The file named by the property 'name' ('/') exists but could not be read.",
            request.getMessage()
        );
    }

    @Test
    public void testIsActiveFileSuccessfullyReadReturnsFalse() throws IOException
    {
        final String contents = "the contents are really cool\nyes they are";

        final ActivationProperty property = new ActivationProperty();

        expect(this.activator.contentsMatch(contents, property, this.problems)).andReturn(false);

        this.replayAll();

        final File temp = File.createTempFile("testIsActiveFileSuccessfullyReadReturnsFalse", ".txt");
        temp.deleteOnExit();
        Files.write(temp.toPath(), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);

        final boolean result = this.activator.isActive(
            temp.getPath(),
            property,
            null,
            this.context,
            this.problems
        );

        this.verifyAll();

        assertFalse(result);
    }

    @Test
    public void testIsActiveFileSuccessfullyReadReturnsTrue() throws IOException
    {
        final String contents = "here are some other interesting contents";

        final ActivationProperty property = new ActivationProperty();

        expect(this.activator.contentsMatch(contents, property, this.problems)).andReturn(true);

        this.replayAll();

        final File temp = File.createTempFile("testIsActiveFileSuccessfullyReadReturnsTrue", ".txt");
        temp.deleteOnExit();
        Files.write(temp.toPath(), contents.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);

        final boolean result = this.activator.isActive(
            temp.getPath(),
            property,
            null,
            this.context,
            this.problems
        );

        this.verifyAll();

        assertTrue(result);
    }

    private static class Base extends BaseFileContentsActivator
    {
        /**
         * Construct a BaseFileContentsActivator.
         *
         * @param logger A Maven logger, auto-injected by Maven
         */
        Base(final Logger logger)
        {
            super(logger);
        }

        @Override
        protected boolean contentsMatch(
            final String contents,
            final ActivationProperty property,
            final ModelProblemCollector problems
        )
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSupportedActivatorBracketName()
        {
            return "MOCKED";
        }
    }
}
