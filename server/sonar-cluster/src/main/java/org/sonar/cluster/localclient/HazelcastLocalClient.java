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

package org.sonar.cluster.localclient;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import org.picocontainer.Startable;
import org.sonar.api.config.Configuration;
import org.sonar.cluster.ClusterObjectKeys;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.sonar.cluster.ClusterProperties.CLUSTER_ENABLED;
import static org.sonar.cluster.ClusterProperties.CLUSTER_LOCALENDPOINT;

/**
 * This class will connect as a Hazelcast client to the local instance of Hazelcluster
 */
public class HazelcastLocalClient implements Startable, HazelcastClient {

  private static final String HAZELCAST_CLUSTER_NAME = "sonarqube";
  private final ClientConfig hzConfig;

  @VisibleForTesting
  HazelcastInstance hzInstance;

  public HazelcastLocalClient(Configuration config) {
    boolean clusterEnabled = config.getBoolean(CLUSTER_ENABLED).orElse(false);
    String clusterLocalEndPoint = config.get(CLUSTER_LOCALENDPOINT).orElse(null);

    checkState(clusterEnabled, "Cluster is not enabled");
    checkState(isNotEmpty(clusterLocalEndPoint), "LocalEndPoint have not been set");

    hzConfig = new ClientConfig();
    hzConfig.getGroupConfig().setName(HAZELCAST_CLUSTER_NAME);
    hzConfig.getNetworkConfig().addAddress(clusterLocalEndPoint);

    // Tweak HazelCast configuration
    hzConfig
      // Increase the number of tries
      .setProperty("hazelcast.tcp.join.port.try.count", "10")
      // Don't phone home
      .setProperty("hazelcast.phone.home.enabled", "false")
      // Use slf4j for logging
      .setProperty("hazelcast.logging.type", "slf4j");
  }

  @Override
  public <E> Set<E> getSet(String name) {
    return hzInstance.getSet(name);
  }

  @Override
  public <E> List<E> getList(String name) {
    return hzInstance.getList(name);
  }

  @Override
  public <K, V> Map<K, V> getMap(String name) {
    return hzInstance.getMap(name);
  }

  @Override
  public <K, V> Map<K, V> getReplicatedMap(String name) {
    return hzInstance.getReplicatedMap(name);
  }

  @Override
  public String getUUID() {
    return hzInstance.getLocalEndpoint().getUuid();
  }

  @Override
  public Set<String> getMemberUuids() {
    ImmutableSet.Builder<String> builder = ImmutableSet.builder();
    builder.addAll(hzInstance.getSet(ClusterObjectKeys.LOCAL_MEMBER_UUIDS));
    hzInstance.getCluster().getMembers().stream().map(Member::getUuid).forEach(builder::add);
    return builder.build();
  }

  @Override
  public Lock getLock(String name) {
    return hzInstance.getLock(name);
  }

  @Override
  public long getClusterTime() {
    return hzInstance.getCluster().getClusterTime();
  }

  @Override
  public void start() {
    this.hzInstance = com.hazelcast.client.HazelcastClient.newHazelcastClient(hzConfig);
  }

  @Override
  public void stop() {
    // Shutdown Hazelcast properly
    hzInstance.shutdown();
  }
}
