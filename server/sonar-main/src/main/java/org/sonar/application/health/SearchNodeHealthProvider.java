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
package org.sonar.application.health;

import org.sonar.NetworkUtils;
import org.sonar.api.utils.System2;
import org.sonar.application.cluster.ClusterAppState;
import org.sonar.cluster.health.NodeDetails;
import org.sonar.cluster.health.NodeHealth;
import org.sonar.cluster.health.NodeHealthProvider;
import org.sonar.process.ProcessId;
import org.sonar.process.Props;

import static org.sonar.cluster.ClusterProperties.CLUSTER_NODE_HOST;
import static org.sonar.cluster.ClusterProperties.CLUSTER_NODE_NAME;
import static org.sonar.cluster.ClusterProperties.CLUSTER_NODE_PORT;

public class SearchNodeHealthProvider implements NodeHealthProvider {
  private final ClusterAppState clusterAppState;
  private final NodeDetails nodeDetails;

  public SearchNodeHealthProvider(Props props, System2 system2, ClusterAppState clusterAppState, NetworkUtils networkUtils) {
    this.clusterAppState = clusterAppState;
    this.nodeDetails = NodeDetails.newNodeDetailsBuilder()
      .setType(NodeDetails.Type.SEARCH)
      .setName(props.nonNullValue(CLUSTER_NODE_NAME))
      .setHost(getHost(props, networkUtils))
      .setPort(Integer.valueOf(props.nonNullValue(CLUSTER_NODE_PORT)))
      .setStartedAt(system2.now())
      .build();
  }

  private static String getHost(Props props, NetworkUtils networkUtils) {
    String host = props.value(CLUSTER_NODE_HOST);
    if (host != null) {
      return host;
    }
    return networkUtils.getHostname();
  }

  @Override
  public NodeHealth get() {
    NodeHealth.Builder builder = NodeHealth.newNodeHealthBuilder();
    if (clusterAppState.isOperational(ProcessId.ELASTICSEARCH, true)) {
      builder.setStatus(NodeHealth.Status.GREEN);
    } else {
      builder.setStatus(NodeHealth.Status.RED)
        .addCause("Elasticsearch is not operational");
    }
    return builder
      .setDetails(nodeDetails)
      .build();
  }
}
