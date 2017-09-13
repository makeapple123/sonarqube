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
import * as React from 'react';
import * as classNames from 'classnames';
import { Link } from 'react-router';
import DateFromNow from '../../../components/intl/DateFromNow';
import DateTimeFormatter from '../../../components/intl/DateTimeFormatter';
import ProjectCardQualityGate from './ProjectCardQualityGate';
import ProjectCardLeakMeasures from './ProjectCardLeakMeasures';
import ProjectCardOrganization from './ProjectCardOrganization';
import Favorite from '../../../components/controls/Favorite';
import TagsList from '../../../components/tags/TagsList';
import PrivateBadge from '../../../components/common/PrivateBadge';
import { translate, translateWithParameters } from '../../../helpers/l10n';
import { Project } from '../types';

interface Props {
  organization?: { key: string };
  project: Project;
}

export default function ProjectCardLeak({ organization, project }: Props) {
  const { measures } = project;

  const isProjectAnalyzed = project.analysisDate != null;
  const isPrivate = project.visibility === 'private';
  const hasLeakPeriodStart = project.leakPeriodDate != undefined;
  const hasTags = project.tags.length > 0;

  // check for particular measures because only some measures can be loaded
  // if coming from visualizations tab
  const areProjectMeasuresLoaded = measures != undefined && measures['new_bugs'];

  const displayQualityGate = areProjectMeasuresLoaded && isProjectAnalyzed;
  const className = classNames('boxed-group', 'project-card', {
    'boxed-group-loading': isProjectAnalyzed && hasLeakPeriodStart && !areProjectMeasuresLoaded
  });

  return (
    <div data-key={project.key} className={className}>
      <div className="boxed-group-header clearfix">
        {project.isFavorite != null && (
          <Favorite
            className="spacer-right"
            component={project.key}
            favorite={project.isFavorite}
          />
        )}
        <h2 className="project-card-name">
          {!organization && <ProjectCardOrganization organization={project.organization} />}
          <Link to={{ pathname: '/dashboard', query: { id: project.key } }}>{project.name}</Link>
        </h2>
        {displayQualityGate && <ProjectCardQualityGate status={measures!['alert_status']} />}
        <div className="pull-right text-right">
          {isPrivate && <PrivateBadge className="spacer-left" tooltipPlacement="left" />}
          {hasTags && <TagsList tags={project.tags} customClass="spacer-left" />}
        </div>
        {isProjectAnalyzed &&
        hasLeakPeriodStart && (
          <div className="project-card-dates note text-right pull-right">
            {hasLeakPeriodStart && (
              <DateFromNow date={project.leakPeriodDate!}>
                {fromNow => (
                  <span className="project-card-leak-date pull-right">
                    {translateWithParameters('projects.leak_period_x', fromNow)}
                  </span>
                )}
              </DateFromNow>
            )}
            {isProjectAnalyzed && (
              <DateTimeFormatter date={project.analysisDate!}>
                {formattedDate => (
                  <span>
                    {translateWithParameters('projects.last_analysis_on_x', formattedDate)}
                  </span>
                )}
              </DateTimeFormatter>
            )}
          </div>
        )}
      </div>

      {isProjectAnalyzed && hasLeakPeriodStart ? (
        <div className="boxed-group-inner">
          {areProjectMeasuresLoaded && <ProjectCardLeakMeasures measures={measures} />}
        </div>
      ) : (
        <div className="boxed-group-inner">
          <div className="note project-card-not-analyzed">
            {isProjectAnalyzed ? (
              translate('projects.no_leak_period')
            ) : (
              translate('projects.not_analyzed')
            )}
          </div>
        </div>
      )}
    </div>
  );
}
