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
package org.sonar.server.health;

import org.junit.Rule;
import org.junit.Test;
import org.sonar.server.es.EsTester;

import static org.assertj.core.api.Assertions.assertThat;

public class EsStatusCheckTest {

  @Rule
  public EsTester esTester = new EsTester();

  private EsStatusCheck underTest = new EsStatusCheck(esTester.client());

  @Test
  public void check_returns_GREEN_without_cause_if_ES_cluster_status_is_GREEN() {
    Health health = underTest.check();

    assertThat(health).isEqualTo(Health.GREEN);
  }

}
