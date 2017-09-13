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

package org.sonar.cluster;

/**
 * This class holds all object keys accessible via Hazelcast
 */
public final class ClusterObjectKeys {

  private ClusterObjectKeys() {
    // Holder for clustered objects
  }

  /**
   * The key of replicated map that hold all operational processes
   */
  public static final String OPERATIONAL_PROCESSES = "OPERATIONAL_PROCESSES";
  /**
   * The key of atomic reference holding the leader UUID
   */
  public static final String LEADER = "LEADER";
  /**
   * The key of the hostname attribute of a member
   */
  public static final String HOSTNAME = "HOSTNAME";
  /**
   * The key of the ips list attribute of a member
   */
  public static final String IP_ADDRESSES = "IP_ADDRESSES";
  /**
   * The key of the node name attribute of a member
   */
  public static final String NODE_NAME = "NODE_NAME";
  /**
   * The role of the sonar-application inside the SonarQube cluster
   * {@link org.sonar.cluster.NodeType}
   */
  public static final String NODE_TYPE = "NODE_TYPE";
  /**
   * The key of atomic reference holding the SonarQube version of the cluster
   */
  public static final String SONARQUBE_VERSION = "SONARQUBE_VERSION";
  /**
   * The key of atomic reference holding the name of the cluster (used for precondition checks)
   */
  public static final String CLUSTER_NAME = "CLUSTER_NAME";
  /**
   * The key of the Set holding the UUIDs of clients
   */
  public static final String LOCAL_MEMBER_UUIDS = "LOCAL_MEMBER_UUIDS";
  /**
   * The key of replicated map holding the CeWorker UUIDs
   */
  public static final String WORKER_UUIDS = "WORKER_UUIDS";
  /**
   * The key of the lock for executing CE_CLEANING_JOB
   * {@link CeCleaningSchedulerImpl}
   */
  public static final String CE_CLEANING_JOB_LOCK = "CE_CLEANING_JOB_LOCK";
  /**
   * THe key of the replicated map holding the health state information of all SQ nodes.
   */
  public static final String SQ_HEALTH_STATE = "sq_health_state";
}
