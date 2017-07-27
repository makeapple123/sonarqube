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
package org.sonar.server.computation.task.projectanalysis.step;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.core.component.ComponentKeys;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.component.SnapshotDto;
import org.sonar.db.component.SnapshotQuery;
import org.sonar.scanner.protocol.output.ScannerReport;
import org.sonar.server.computation.task.projectanalysis.analysis.Analysis;
import org.sonar.server.computation.task.projectanalysis.analysis.MutableAnalysisMetadataHolder;
import org.sonar.server.computation.task.projectanalysis.batch.BatchReportReader;
import org.sonar.server.computation.task.projectanalysis.component.Component;
import org.sonar.server.computation.task.projectanalysis.component.ComponentKeyGenerator;
import org.sonar.server.computation.task.projectanalysis.component.ComponentTreeBuilder;
import org.sonar.server.computation.task.projectanalysis.component.ComponentUuidFactory;
import org.sonar.server.computation.task.projectanalysis.component.MutableTreeRootHolder;
import org.sonar.server.computation.task.step.ComputationStep;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * Populates the {@link MutableTreeRootHolder} and {@link MutableAnalysisMetadataHolder} from the {@link BatchReportReader}
 */
public class BuildComponentTreeStep implements ComputationStep {

  private final DbClient dbClient;
  private final BatchReportReader reportReader;
  private final MutableTreeRootHolder treeRootHolder;
  private final MutableAnalysisMetadataHolder analysisMetadataHolder;

  public BuildComponentTreeStep(DbClient dbClient, BatchReportReader reportReader,
    MutableTreeRootHolder treeRootHolder, MutableAnalysisMetadataHolder analysisMetadataHolder) {
    this.dbClient = dbClient;
    this.reportReader = reportReader;
    this.treeRootHolder = treeRootHolder;
    this.analysisMetadataHolder = analysisMetadataHolder;
  }

  @Override
  public String getDescription() {
    return "Build tree of components";
  }

  @Override
  public void execute() {
    try (DbSession dbSession = dbClient.openSession(false)) {
      ScannerReport.Component reportProject = reportReader.readComponent(analysisMetadataHolder.getRootComponentRef());
      ComponentKeyGenerator keyGenerator = loadKeyGenerator();

      // root key of branch, not necessarily of project
      String rootKey = keyGenerator.generateKey(reportProject, null);

      // loads the UUIDs from database. If they don't exist, then generate new ones
      ComponentUuidFactory componentUuidFactory = new ComponentUuidFactory(dbClient, dbSession, rootKey);

      String rootUuid = componentUuidFactory.getOrCreateForKey(rootKey);
      SnapshotDto baseAnalysis = loadBaseAnalysis(dbSession, rootUuid);

      ComponentTreeBuilder builder = new ComponentTreeBuilder(keyGenerator,
        componentUuidFactory::getOrCreateForKey,
        reportReader::readComponent,
        analysisMetadataHolder.getProject(),
        baseAnalysis);
      Component project = builder.buildProject(reportProject);

      treeRootHolder.setRoot(project);
      analysisMetadataHolder.setBaseAnalysis(toAnalysis(baseAnalysis));
    }
  }

  private ComponentKeyGenerator loadKeyGenerator() {
    return Stream.of(analysisMetadataHolder.getBranch(), Optional.of(new DefaultKeyGenerator()))
      // TODO pull request generator will be added here
      .filter(Optional::isPresent)
      .flatMap(x -> x.map(Stream::of).orElseGet(Stream::empty))
      .findFirst()
      .get();
  }

  @CheckForNull
  private SnapshotDto loadBaseAnalysis(DbSession dbSession, String rootUuid) {
    return dbClient.snapshotDao().selectAnalysisByQuery(
      dbSession,
      new SnapshotQuery()
        .setComponentUuid(rootUuid)
        .setIsLast(true));
  }

  @CheckForNull
  private static Analysis toAnalysis(@Nullable SnapshotDto dto) {
    if (dto != null) {
      return new Analysis.Builder()
        .setId(dto.getId())
        .setUuid(dto.getUuid())
        .setCreatedAt(dto.getCreatedAt())
        .build();
    }
    return null;
  }

  private static class DefaultKeyGenerator implements ComponentKeyGenerator {
    @Override
    public String generateKey(ScannerReport.Component module, @Nullable ScannerReport.Component fileOrDir) {
      String moduleKey =  module.getKey();
      if (fileOrDir == null || isEmpty(fileOrDir.getPath())) {
        return moduleKey;
      }
      return ComponentKeys.createEffectiveKey(moduleKey, trimToNull(fileOrDir.getPath()));
    }
  }
}
