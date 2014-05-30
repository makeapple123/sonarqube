/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.qualityprofile.ws;

import org.sonar.api.server.ws.WebService;

public class QProfilesWs implements WebService {

  public static final String API_ENDPOINT = "api/qualityprofiles";

  private final QProfileRecreateBuiltInAction recreateBuiltInAction;
  private final RuleActivationActions ruleActivationActions;
  private final BulkRuleActivationActions bulkRuleActivationActions;
  private final RuleResetAction ruleResetAction;

  public QProfilesWs(QProfileRecreateBuiltInAction recreateBuiltInAction,
                     RuleActivationActions ruleActivationActions,
                     BulkRuleActivationActions bulkRuleActivationActions,
                     RuleResetAction ruleResetAction) {
    this.recreateBuiltInAction = recreateBuiltInAction;
    this.ruleActivationActions = ruleActivationActions;
    this.bulkRuleActivationActions = bulkRuleActivationActions;
    this.ruleResetAction = ruleResetAction;
  }

  @Override
  public void define(Context context) {
    NewController controller = context.createController(API_ENDPOINT)
      .setDescription("Quality profiles management")
      .setSince("4.4");

    recreateBuiltInAction.define(controller);
    ruleActivationActions.define(controller);
    bulkRuleActivationActions.define(controller);
    ruleResetAction.define(controller);

    controller.done();
  }
}
