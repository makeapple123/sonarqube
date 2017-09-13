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
package org.sonar.application.cluster;

import com.hazelcast.core.HazelcastInstance;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.DisableOnDebug;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.sonar.application.AppStateListener;
import org.sonar.application.config.TestAppSettings;
import org.sonar.process.MessageException;
import org.sonar.process.ProcessId;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.sonar.application.cluster.HazelcastClusterTestHelper.createHazelcastClient;
import static org.sonar.application.cluster.HazelcastClusterTestHelper.newApplicationSettings;
import static org.sonar.cluster.ClusterObjectKeys.CLUSTER_NAME;
import static org.sonar.cluster.ClusterObjectKeys.SONARQUBE_VERSION;
import static org.sonar.cluster.ClusterProperties.CLUSTER_ENABLED;

public class ClusterAppStateImplTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Rule
  public TestRule safeguardTimeout = new DisableOnDebug(Timeout.seconds(60));

  @Test
  public void instantiation_throws_ISE_if_cluster_mode_is_disabled() throws Exception {
    TestAppSettings settings = new TestAppSettings();
    settings.set(CLUSTER_ENABLED, "false");

    expectedException.expect(IllegalStateException.class);
    expectedException.expectMessage("Cluster is not enabled on this instance");

    new ClusterAppStateImpl(settings);
  }

  @Test
  public void tryToLockWebLeader_returns_true_only_for_the_first_call() throws Exception {
    TestAppSettings settings = newApplicationSettings();

    try (ClusterAppStateImpl underTest = new ClusterAppStateImpl(settings)) {
      assertThat(underTest.tryToLockWebLeader()).isEqualTo(true);
      assertThat(underTest.tryToLockWebLeader()).isEqualTo(false);
    }
  }

  @Test
  public void log_when_sonarqube_is_joining_a_cluster() throws IOException, InterruptedException, IllegalAccessException, NoSuchFieldException {
    // Now launch an instance that try to be part of the hzInstance cluster
    TestAppSettings settings = newApplicationSettings();

    Logger logger = mock(Logger.class);
    ClusterAppStateImpl.setLogger(logger);

    try (ClusterAppStateImpl appStateCluster = new ClusterAppStateImpl(settings)) {
      verify(logger).info(
        eq("Joined a SonarQube cluster that contains the following hosts : [{}]"),
        anyString());
    }
  }

  @Test
  public void test_listeners() throws InterruptedException {
    AppStateListener listener = mock(AppStateListener.class);
    try (ClusterAppStateImpl underTest = new ClusterAppStateImpl(newApplicationSettings())) {
      underTest.addListener(listener);

      underTest.setOperational(ProcessId.ELASTICSEARCH);
      verify(listener, timeout(20_000)).onAppStateOperational(ProcessId.ELASTICSEARCH);

      assertThat(underTest.isOperational(ProcessId.ELASTICSEARCH, true)).isEqualTo(true);
      assertThat(underTest.isOperational(ProcessId.APP, true)).isEqualTo(false);
      assertThat(underTest.isOperational(ProcessId.WEB_SERVER, true)).isEqualTo(false);
      assertThat(underTest.isOperational(ProcessId.COMPUTE_ENGINE, true)).isEqualTo(false);
    }
  }

  @Test
  public void registerSonarQubeVersion_publishes_version_on_first_call() {
    TestAppSettings settings = newApplicationSettings();

    try (ClusterAppStateImpl appStateCluster = new ClusterAppStateImpl(settings)) {
      appStateCluster.registerSonarQubeVersion("6.4.1.5");

      HazelcastInstance hzInstance = createHazelcastClient(appStateCluster);
      assertThat(hzInstance.getAtomicReference(SONARQUBE_VERSION).get())
        .isNotNull()
        .isInstanceOf(String.class)
        .isEqualTo("6.4.1.5");
    }
  }

  @Test
  public void registerClusterName_publishes_clusterName_on_first_call() {
    TestAppSettings settings = newApplicationSettings();
    String clusterName = randomAlphanumeric(20);

    try (ClusterAppStateImpl appStateCluster = new ClusterAppStateImpl(settings)) {
      appStateCluster.registerClusterName(clusterName);

      HazelcastInstance hzInstance = createHazelcastClient(appStateCluster);
      assertThat(hzInstance.getAtomicReference(CLUSTER_NAME).get())
        .isNotNull()
        .isInstanceOf(String.class)
        .isEqualTo(clusterName);
    }
  }

  @Test
  public void reset_throws_always_ISE() {
    TestAppSettings settings = newApplicationSettings();

    try (ClusterAppStateImpl appStateCluster = new ClusterAppStateImpl(settings)) {
      expectedException.expect(IllegalStateException.class);
      expectedException.expectMessage("state reset is not supported in cluster mode");
      appStateCluster.reset();
    }
  }

  @Test
  public void registerSonarQubeVersion_throws_ISE_if_initial_version_is_different() throws Exception {
    // Now launch an instance that try to be part of the hzInstance cluster
    TestAppSettings settings = newApplicationSettings();

    try (ClusterAppStateImpl appStateCluster = new ClusterAppStateImpl(settings)) {
      // Register first version
      appStateCluster.registerSonarQubeVersion("1.0.0");

      expectedException.expect(IllegalStateException.class);
      expectedException.expectMessage("The local version 2.0.0 is not the same as the cluster 1.0.0");

      // Registering a second different version must trigger an exception
      appStateCluster.registerSonarQubeVersion("2.0.0");
    }
  }

  @Test
  public void registerClusterName_throws_MessageException_if_clusterName_is_different() throws Exception {
    // Now launch an instance that try to be part of the hzInstance cluster
    TestAppSettings settings = newApplicationSettings();

    try (ClusterAppStateImpl appStateCluster = new ClusterAppStateImpl(settings)) {
      // Register first version
      appStateCluster.registerClusterName("goodClusterName");

      expectedException.expect(MessageException.class);
      expectedException.expectMessage("This node has a cluster name [badClusterName], which does not match [goodClusterName] from the cluster");

      // Registering a second different cluster name must trigger an exception
      appStateCluster.registerClusterName("badClusterName");
    }
  }
}
