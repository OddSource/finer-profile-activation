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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.apache.maven.model.profile.activation.PropertyProfileActivator;
import org.codehaus.plexus.logging.Logger;
import org.easymock.EasyMockRule;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import io.oddsource.java.maven.profile.FinerActivator;
import io.oddsource.java.maven.profile.FinerProfileSelector;

public class TestFinerProfileSelector extends EasyMockSupport
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

    @Mock
    private ProfileActivator standard1;

    @Mock
    private ProfileActivator standard2;

    private PropertyProfileActivator propertyActivator;

    @Mock
    private FinerActivator finer1;

    @Mock
    private FinerActivator finer2;

    private TestableFinerProfileSelector selector;

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

        this.propertyActivator = this.createMockBuilder(PropertyProfileActivator.class).
            addMockedMethod(
                "presentInConfig",
                Profile.class,
                ProfileActivationContext.class,
                ModelProblemCollector.class
            ).
            addMockedMethod(
                "isActive",
                Profile.class,
                ProfileActivationContext.class,
                ModelProblemCollector.class
            ).mock();

        this.selector = this.createMockBuilder(TestableFinerProfileSelector.class).
            addMockedMethod(
                "getSupersActiveProfiles",
                Collection.class,
                ProfileActivationContext.class,
                ModelProblemCollector.class).
            withConstructor(Logger.class, List.class, List.class).
            withArgs(
                this.logger,
                Arrays.asList(this.standard1, this.standard2, this.propertyActivator),
                Arrays.asList(this.finer1, this.finer2)
            ).
            mock();

        reset(this.logger);
        this.setUpLogger();
    }

    @Test
    public void testGetActiveProfilesOneSuperActiveNothingPresentInConfig()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>(Collections.singletonList(profile1))).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(false).once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(1, active.size());
        assertEquals(profile1, active.get(0));

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActiveNothingPresentInConfig()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(false).once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(0, active.size());

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActiveNonPropertyTypePresentInConfigNotActive()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(true).once();
        expect(this.standard2.isActive(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(false).once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(0, active.size());

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActiveNonPropertyTypeActive()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(true).once();
        expect(this.standard2.isActive(profile2, this.context, this.problems)).andReturn(true).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(false).once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(1, active.size());
        assertEquals(profile2, active.get(0));

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesOneSuperActiveNonPropertyTypeActive()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>(Collections.singletonList(profile2))).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(true).once();
        expect(this.standard2.isActive(profile1, this.context, this.problems)).andReturn(true).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(2, active.size());
        assertEquals(profile2, active.get(0));
        assertEquals(profile1, active.get(1));

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActivePropertyTypeActive()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(true).once();
        expect(this.propertyActivator.isActive(profile2, this.context, this.problems)).andReturn(true).once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(1, active.size());
        assertEquals(profile2, active.get(0));

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActivePropertyTypePresentNotActiveNullName()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(true).once();
        expect(this.propertyActivator.isActive(profile2, this.context, this.problems)).andReturn(false).once();

        profile2.setActivation(new Activation());
        profile2.getActivation().setProperty(new ActivationProperty());

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(0, active.size());

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActivePropertyTypePresentNotActiveNameNotMatching()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(true).once();
        expect(this.propertyActivator.isActive(profile2, this.context, this.problems)).andReturn(false).once();

        profile2.setActivation(new Activation());
        profile2.getActivation().setProperty(new ActivationProperty());
        profile2.getActivation().getProperty().setName("this does not have our special prefix");

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(0, active.size());

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActivePropertyTypePresentNotActiveNameUnsupported()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(true).once();
        expect(this.propertyActivator.isActive(profile2, this.context, this.problems)).andReturn(false).once();

        profile2.setActivation(new Activation());
        profile2.getActivation().setProperty(new ActivationProperty());
        profile2.getActivation().getProperty().setName("[FINER.ACTIVATOR.UNSUPPORTED]this is not supported");

        expect(this.finer1.getSupportedActivatorBracketName()).andReturn("FOO").once();
        expect(this.finer2.getSupportedActivatorBracketName()).andReturn("BAR").once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(0, active.size());

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActiveFinerActivatorNotActive()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(true).once();
        expect(this.propertyActivator.isActive(profile2, this.context, this.problems)).andReturn(false).once();

        profile2.setActivation(new Activation());
        profile2.getActivation().setProperty(new ActivationProperty());
        profile2.getActivation().getProperty().setName("[FINER.ACTIVATOR.FOO]this is not active");

        expect(this.finer1.getSupportedActivatorBracketName()).andReturn("FOO").once();
        expect(this.finer1.isActive(
            "this is not active",
            profile2.getActivation().getProperty(),
            profile2,
            this.context,
            this.problems
        )).andReturn(false).once();
        expect(this.finer2.getSupportedActivatorBracketName()).andReturn("BAR").once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(0, active.size());

        this.verifyAll();
    }

    @Test
    public void testGetActiveProfilesNothingSuperActiveFinerActivatorOneActive()
    {
        final Profile profile1 = new Profile();
        profile1.setId("profile1");
        final Profile profile2 = new Profile();
        profile2.setId("profile2");

        final List<Profile> profiles = Arrays.asList(profile1, profile2);

        expect(this.selector.getSupersActiveProfiles(profiles, this.context, this.problems)).
            andReturn(new ArrayList<>()).once();

        expect(this.standard1.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile1, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile1, this.context, this.problems)).
            andReturn(false).once();

        expect(this.standard1.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.standard2.presentInConfig(profile2, this.context, this.problems)).andReturn(false).once();
        expect(this.propertyActivator.presentInConfig(profile2, this.context, this.problems)).
            andReturn(true).once();
        expect(this.propertyActivator.isActive(profile2, this.context, this.problems)).andReturn(false).once();

        profile2.setActivation(new Activation());
        profile2.getActivation().setProperty(new ActivationProperty());
        profile2.getActivation().getProperty().setName("[FINER.ACTIVATOR.BAR]this is active");

        expect(this.finer1.getSupportedActivatorBracketName()).andReturn("FOO").once();
        expect(this.finer2.getSupportedActivatorBracketName()).andReturn("BAR").once();
        expect(this.finer2.isActive(
            "this is active",
            profile2.getActivation().getProperty(),
            profile2,
            this.context,
            this.problems
        )).andReturn(true).once();

        this.replayAll();

        final List<Profile> active = this.selector.getActiveProfiles(profiles, this.context, this.problems);
        assertNotNull(active);
        assertEquals(1, active.size());
        assertEquals(profile2, active.get(0));

        this.verifyAll();
    }

    protected static class TestableFinerProfileSelector extends FinerProfileSelector
    {
        /**
         * Construct a selector.
         *
         * @param logger A Maven logger
         * @param standardActivators All built-in activators
         * @param finerActivators All of our activators
         */
        public TestableFinerProfileSelector(
            final Logger logger,
            final List<ProfileActivator> standardActivators,
            final List<FinerActivator> finerActivators
        )
        {
            super(logger, standardActivators, finerActivators);
        }

        @Override
        public List<Profile> getSupersActiveProfiles(
            final Collection<Profile> profiles,
            final ProfileActivationContext context,
            final ModelProblemCollector problems
        )
        {
            throw new UnsupportedOperationException("Because this should always be mocked.");
        }
    }
}
