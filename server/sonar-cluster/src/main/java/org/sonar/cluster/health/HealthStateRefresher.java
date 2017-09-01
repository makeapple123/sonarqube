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
package org.sonar.cluster.health;

import java.util.concurrent.TimeUnit;
import org.picocontainer.Startable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class HealthStateRefresher implements Startable {
  private static final Logger LOG = Loggers.get(HealthStateRefresher.class);
  private static final int INITIAL_DELAY = 1;
  private static final int DELAY = 10;

  private final HealthStateRefresherExecutorService executorService;
  private final NodeHealthProvider nodeHealthProvider;
  private final SharedHealthState sharedHealthState;

  public HealthStateRefresher(HealthStateRefresherExecutorService executorService, NodeHealthProvider nodeHealthProvider,
    SharedHealthState sharedHealthState) {
    this.executorService = executorService;
    this.nodeHealthProvider = nodeHealthProvider;
    this.sharedHealthState = sharedHealthState;
  }

  @Override
  public void start() {
    executorService.scheduleWithFixedDelay(this::refresh, INITIAL_DELAY, DELAY, TimeUnit.SECONDS);
  }

  private void refresh() {
    try {
      NodeHealth nodeHealth = nodeHealthProvider.get();
      sharedHealthState.writeMine(nodeHealth);
    } catch (Throwable t) {
      LOG.error("An error occurred while attempting to refresh HealthState of the current node in the shared state:", t);
    }
  }

  @Override
  public void stop() {
    sharedHealthState.clearMine();
  }
}
