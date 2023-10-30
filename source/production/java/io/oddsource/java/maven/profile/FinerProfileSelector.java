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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.ActivationProperty;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.profile.DefaultProfileSelector;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.apache.maven.model.profile.activation.ProfileActivator;
import org.apache.maven.model.profile.activation.PropertyProfileActivator;
import org.codehaus.plexus.logging.Logger;

/**
 * Maven automatically constructs this selector to overwrite the default profile selector,
 * and since it extends the {@link DefaultProfileSelector}, all the rules that selector applies
 * are also applied here.
 *
 * @since 1.0.0
 */
@Named("default")
@Singleton
public class FinerProfileSelector extends DefaultProfileSelector
{
    private static final Pattern ACTIVATOR_PATTERN = Pattern.compile(
        "^\\[FINER\\.ACTIVATOR\\.(?<activator>[A-Z0-9.]+)](?<property>.*)$"
    );

    /**
     * Injected by Maven.
     */
    private final Logger logger;

    /**
     * Injected by Maven.
     */
    private final List<ProfileActivator> standardActivators = new ArrayList<>();

    /**
     * Injected by Maven.
     */
    private final List<FinerActivator> finerActivators = new ArrayList<>();

    /**
     * Construct a selector.
     *
     * @param logger A Maven logger, auto-injected by Maven
     * @param standardActivators All built-in activators defined by Maven, auto-injected by Maven
     * @param finerActivators All activators defined in io.oddsource.java.maven.profile, auto-injected by Maven
     */
    @Inject
    public FinerProfileSelector(
        final Logger logger,
        final List<ProfileActivator> standardActivators,
        final List<FinerActivator> finerActivators
    )
    {
        this.logger = logger;
        this.standardActivators.addAll(standardActivators);
        this.finerActivators.addAll(finerActivators);

        if(this.logger.isDebugEnabled())
        {
            this.logger.debug(
                "FinerProfileSelector constructed with " + standardActivators.size() + " standard activators and" +
                finerActivators.size() + " finer activators."
            );
        }
    }

    @Override
    public List<Profile> getActiveProfiles(
        final Collection<Profile> profiles,
        final ProfileActivationContext context,
        final ModelProblemCollector problems
    )
    {
        final List<Profile> activeList = this.getSupersActiveProfiles(profiles, context, problems);
        final Set<String> activeSet = new HashSet<>(activeList.size());
        activeList.forEach(profile -> {
            if(this.logger.isDebugEnabled())
            {
                this.logger.debug("Profile " + profile.getId() + " activated by DefaultProfileSelector.");
            }
            activeSet.add(profile.getId());
        });

        for(final Profile profile : profiles)
        {
            // only test the profile for activation if the super has not already activated it
            if(!activeSet.contains(profile.getId()))
            {
                if(this.logger.isDebugEnabled())
                {
                    this.logger.debug("Checking inactive profile " + profile.getId() + " for activatability.");
                }
                if(this.isActive(profile, context, problems))
                {
                    activeList.add(profile);
                }
            }
        }

        if(this.logger.isDebugEnabled() && !activeList.isEmpty())
        {
            this.logger.info("Activator selected profiles: " + Arrays.toString(activeList.toArray()));
        }

        return activeList;
    }

    /**
     * For test-mocking purposes, a wrapper around {@code super.getActiveProfiles(...)}, and
     * {@link #getActiveProfiles(Collection, ProfileActivationContext, ModelProblemCollector)} calls this method
     * instead of {@code super.getActiveProfiles(...)}.
     *
     * @param profiles The profiles
     * @param context The context
     * @param problems A collector of problems
     * @return the result of the call to {@code super.getActiveProfiles(...)}.
     */
    protected List<Profile> getSupersActiveProfiles(
        final Collection<Profile> profiles,
        final ProfileActivationContext context,
        final ModelProblemCollector problems
    )
    {
        // for testing purposes, we separate out this super call so that we can mock it
        return super.getActiveProfiles(profiles, context, problems);
    }

    private boolean isActive(
        final Profile profile,
        final ProfileActivationContext context,
        final ModelProblemCollector problems
    )
    {
        int countPresent = 0;
        int countActive = 0;

        for(final ProfileActivator activator : this.standardActivators)
        {
            if(activator.presentInConfig(profile, context, problems))
            {
                countPresent++;
                final boolean active = activator.isActive(profile, context, problems);
                if(this.logger.isDebugEnabled())
                {
                    this.logger.debug("Standard activator " + activator + " " + this.activeInactive(active));
                }
                if(active)
                {
                    countActive++;
                }
                else if(activator instanceof PropertyProfileActivator &&
                        this.isFinerActive(profile, context, problems))
                {
                    countActive++;
                }
            }
        }

        return countActive > 0 && countActive == countPresent;
    }

    private boolean isFinerActive(
        final Profile profile,
        final ProfileActivationContext context,
        final ModelProblemCollector problems
    )
    {
        final ActivationProperty property = profile.getActivation().getProperty();
        final String name = property.getName();
        if(name == null)
        {
            // the PropertyProfileActivator class will have already registered a problem
            return false;
        }

        int countPresent = 0;
        int countActive = 0;

        final Matcher matcher = FinerProfileSelector.ACTIVATOR_PATTERN.matcher(name);
        if(matcher.matches())
        {
            if(this.logger.isDebugEnabled())
            {
                this.logger.debug("Property name '" + name + "' matches finer activator pattern.");
            }
            for(final FinerActivator finer : this.finerActivators)
            {
                final boolean applies = finer.getSupportedActivatorBracketName().equals(matcher.group("activator"));
                final boolean active = applies && finer.isActive(
                    matcher.group("property"), property, profile, context, problems
                );
                if(this.logger.isDebugEnabled())
                {
                    this.logger.debug(finer + " applicable to property name '" + name + "' = " + applies);
                    this.logger.debug(finer + " " + this.activeInactive(active));
                }
                if(applies)
                {
                    countPresent++;
                }
                if(active)
                {
                    countActive++;
                }
            }
        }

        return countActive > 0 && countActive == countPresent;
    }

    private String activeInactive(final boolean active)
    {
        return active ? "ACTIVE" : "INACTIVE";
    }
}
