/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.qualityprofile;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import java.util.List;
import java.util.Locale;
import org.sonar.api.profiles.ProfileDefinition;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRuleParam;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.ValidationMessages;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.log.Profiler;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.lowerCase;

/**
 * Bridge between deprecated {@link ProfileDefinition} API and new {@link BuiltInQualityProfilesDefinition}
 */
public class BuiltInQProfileDefinitionsBridge implements BuiltInQualityProfilesDefinition {
  private static final Logger LOGGER = Loggers.get(BuiltInQProfileDefinitionsBridge.class);
  private static final String DEFAULT_PROFILE_NAME = "Sonar way";

  private final List<ProfileDefinition> definitions;

  /**
   * Requires for pico container when no {@link ProfileDefinition} is defined at all
   */
  public BuiltInQProfileDefinitionsBridge() {
    this(new ProfileDefinition[0]);
  }

  public BuiltInQProfileDefinitionsBridge(ProfileDefinition... definitions) {
    this.definitions = ImmutableList.copyOf(definitions);
  }

  @Override
  public void define(Context context) {
    ListMultimap<String, RulesProfile> rulesProfilesByLanguage = buildRulesProfilesByLanguage();
    for (String language : rulesProfilesByLanguage.keySet()) {
      for (RulesProfile profile : rulesProfilesByLanguage.get(language)) {
        NewBuiltInQualityProfile newQp = context.createBuiltInQualityProfile(profile.getName(), language)
          .setDefault(profile.getName().equals(DEFAULT_PROFILE_NAME));

        for (org.sonar.api.rules.ActiveRule ar : profile.getActiveRules()) {
          NewBuiltInActiveRule newActiveRule = newQp.activateRule(ar.getRepositoryKey(), ar.getRuleKey());
          RulePriority overridenSeverity = ar.getOverridenSeverity();
          if (overridenSeverity != null) {
            newActiveRule.overrideSeverity(overridenSeverity.name());
          }
          for (ActiveRuleParam param : ar.getActiveRuleParams()) {
            newActiveRule.overrideParam(param.getKey(), param.getValue());
          }
        }

        newQp.done();
      }
    }
  }

  /**
   * @return profiles by language
   */
  private ListMultimap<String, RulesProfile> buildRulesProfilesByLanguage() {
    ListMultimap<String, RulesProfile> byLang = ArrayListMultimap.create();
    Profiler profiler = Profiler.create(Loggers.get(getClass()));
    for (ProfileDefinition definition : definitions) {
      profiler.start();
      ValidationMessages validation = ValidationMessages.create();
      RulesProfile profile = definition.createProfile(validation);
      validation.log(LOGGER);
      if (profile == null) {
        profiler.stopDebug(format("Loaded definition %s that return no profile", definition));
      } else {
        if (!validation.hasErrors()) {
          checkArgument(isNotEmpty(profile.getName()), "Profile created by Definition %s can't have a blank name", definition);
          byLang.put(lowerCase(profile.getLanguage(), Locale.ENGLISH), profile);
        }
        profiler.stopDebug(format("Loaded deprecated profile definition %s for language %s", profile.getName(), profile.getLanguage()));
      }
    }
    return byLang;
  }

}
