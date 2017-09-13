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
import { Link } from 'react-router';
import * as classNames from 'classnames';
import * as PropTypes from 'prop-types';
import { Branch, Component, ComponentExtension, ComponentConfiguration } from '../../../types';
import NavBarTabs from '../../../../components/nav/NavBarTabs';
import {
  isShortLivingBranch,
  getBranchName,
  isLongLivingBranch
} from '../../../../helpers/branches';
import { translate } from '../../../../helpers/l10n';

const SETTINGS_URLS = [
  '/project/admin',
  '/project/branches',
  '/project/settings',
  '/project/quality_profiles',
  '/project/quality_gate',
  '/custom_measures',
  '/project/links',
  '/project_roles',
  '/project/history',
  'background_tasks',
  '/project/key',
  '/project/deletion'
];

interface Props {
  branch?: Branch;
  component: Component;
  conf: ComponentConfiguration;
  location?: any;
}

export default class ComponentNavMenu extends React.PureComponent<Props> {
  static contextTypes = {
    branchesEnabled: PropTypes.bool.isRequired
  };

  isProject() {
    return this.props.component.qualifier === 'TRK';
  }

  isDeveloper() {
    return this.props.component.qualifier === 'DEV';
  }

  isView() {
    const { qualifier } = this.props.component;
    return qualifier === 'VW' || qualifier === 'SVW';
  }

  isApplication() {
    return this.props.component.qualifier === 'APP';
  }

  renderDashboardLink() {
    if (this.props.branch && isShortLivingBranch(this.props.branch)) {
      return null;
    }

    const pathname = this.isView() ? '/portfolio' : '/dashboard';
    return (
      <li>
        <Link
          to={{
            pathname,
            query: {
              branch: this.props.branch && getBranchName(this.props.branch),
              id: this.props.component.key
            }
          }}
          activeClassName="active">
          {translate('overview.page')}
        </Link>
      </li>
    );
  }

  renderCodeLink() {
    if (this.isDeveloper()) {
      return null;
    }

    return (
      <li>
        <Link
          to={{
            pathname: '/code',
            query: {
              branch: this.props.branch && getBranchName(this.props.branch),
              id: this.props.component.key
            }
          }}
          activeClassName="active">
          {this.isView() || this.isApplication() ? (
            translate('view_projects.page')
          ) : (
            translate('code.page')
          )}
        </Link>
      </li>
    );
  }

  renderActivityLink() {
    if (!this.isProject() && !this.isApplication()) {
      return null;
    }

    if (this.props.branch && isShortLivingBranch(this.props.branch)) {
      return null;
    }

    return (
      <li>
        <Link
          to={{
            pathname: '/project/activity',
            query: {
              branch: this.props.branch && getBranchName(this.props.branch),
              id: this.props.component.key
            }
          }}
          activeClassName="active">
          {translate('project_activity.page')}
        </Link>
      </li>
    );
  }

  renderIssuesLink() {
    return (
      <li>
        <Link
          to={{
            pathname: '/project/issues',
            query: {
              branch: this.props.branch && getBranchName(this.props.branch),
              id: this.props.component.key,
              resolved: 'false'
            }
          }}
          activeClassName="active">
          {translate('issues.page')}
        </Link>
      </li>
    );
  }

  renderComponentMeasuresLink() {
    if (this.props.branch && isShortLivingBranch(this.props.branch)) {
      return null;
    }

    return (
      <li>
        <Link
          to={{
            pathname: '/component_measures',
            query: {
              branch: this.props.branch && getBranchName(this.props.branch),
              id: this.props.component.key
            }
          }}
          activeClassName="active">
          {translate('layout.measures')}
        </Link>
      </li>
    );
  }

  renderAdministration() {
    const { branch } = this.props;

    if (!this.props.conf.showSettings || (branch && isShortLivingBranch(branch))) {
      return null;
    }

    const isSettingsActive = SETTINGS_URLS.some(url => window.location.href.indexOf(url) !== -1);

    if (branch && isLongLivingBranch(branch)) {
      return (
        <li>
          <Link
            className={classNames('is-admin', { active: isSettingsActive })}
            id="component-navigation-admin"
            to={{
              pathname: '/project/settings',
              query: { branch: getBranchName(branch), id: this.props.component.key }
            }}>
            {translate('layout.settings')}&nbsp;
          </Link>
        </li>
      );
    }

    const adminLinks = this.renderAdministrationLinks();
    if (!adminLinks.some(link => link != null)) {
      return null;
    }

    return (
      <li className="dropdown">
        <a
          className={classNames('dropdown-toggle', 'is-admin', { active: isSettingsActive })}
          id="component-navigation-admin"
          data-toggle="dropdown"
          href="#">
          {translate('layout.settings')}&nbsp;
          <i className="icon-dropdown" />
        </a>
        <ul className="dropdown-menu">{adminLinks}</ul>
      </li>
    );
  }

  renderAdministrationLinks() {
    return [
      this.renderSettingsLink(),
      this.renderBranchesLink(),
      this.renderProfilesLink(),
      this.renderQualityGateLink(),
      this.renderCustomMeasuresLink(),
      this.renderLinksLink(),
      this.renderPermissionsLink(),
      this.renderBackgroundTasksLink(),
      this.renderUpdateKeyLink(),
      ...this.renderAdminExtensions(),
      this.renderDeletionLink()
    ];
  }

  renderSettingsLink() {
    if (!this.props.conf.showSettings || this.isApplication() || this.isView()) {
      return null;
    }
    return (
      <li key="settings">
        <Link
          to={{
            pathname: '/project/settings',
            query: {
              branch: this.props.branch && getBranchName(this.props.branch),
              id: this.props.component.key
            }
          }}
          activeClassName="active">
          {translate('project_settings.page')}
        </Link>
      </li>
    );
  }

  renderBranchesLink() {
    if (!this.context.branchesEnabled || !this.isProject() || !this.props.conf.showSettings) {
      return null;
    }
    return (
      <li key="branches">
        <Link
          to={{ pathname: '/project/branches', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('project_branches.page')}
        </Link>
      </li>
    );
  }

  renderProfilesLink() {
    if (!this.props.conf.showQualityProfiles) {
      return null;
    }
    return (
      <li key="profiles">
        <Link
          to={{ pathname: '/project/quality_profiles', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('project_quality_profiles.page')}
        </Link>
      </li>
    );
  }

  renderQualityGateLink() {
    if (!this.props.conf.showQualityGates) {
      return null;
    }
    return (
      <li key="quality_gate">
        <Link
          to={{ pathname: '/project/quality_gate', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('project_quality_gate.page')}
        </Link>
      </li>
    );
  }

  renderCustomMeasuresLink() {
    if (!this.props.conf.showManualMeasures) {
      return null;
    }
    return (
      <li key="custom_measures">
        <Link
          to={{ pathname: '/custom_measures', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('custom_measures.page')}
        </Link>
      </li>
    );
  }

  renderLinksLink() {
    if (!this.props.conf.showLinks) {
      return null;
    }
    return (
      <li key="links">
        <Link
          to={{ pathname: '/project/links', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('project_links.page')}
        </Link>
      </li>
    );
  }

  renderPermissionsLink() {
    if (!this.props.conf.showPermissions) {
      return null;
    }
    return (
      <li key="permissions">
        <Link
          to={{ pathname: '/project_roles', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('permissions.page')}
        </Link>
      </li>
    );
  }

  renderBackgroundTasksLink() {
    if (!this.props.conf.showBackgroundTasks) {
      return null;
    }
    return (
      <li key="background_tasks">
        <Link
          to={{ pathname: '/project/background_tasks', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('background_tasks.page')}
        </Link>
      </li>
    );
  }

  renderUpdateKeyLink() {
    if (!this.props.conf.showUpdateKey) {
      return null;
    }
    return (
      <li key="update_key">
        <Link
          to={{ pathname: '/project/key', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('update_key.page')}
        </Link>
      </li>
    );
  }

  renderDeletionLink() {
    const { qualifier } = this.props.component;

    if (!this.props.conf.showSettings) {
      return null;
    }

    if (qualifier !== 'TRK' && qualifier !== 'VW' && qualifier !== 'APP') {
      return null;
    }

    return (
      <li key="project_delete">
        <Link
          to={{ pathname: '/project/deletion', query: { id: this.props.component.key } }}
          activeClassName="active">
          {translate('deletion.page')}
        </Link>
      </li>
    );
  }

  renderExtension = ({ key, name }: ComponentExtension, isAdmin: boolean) => {
    const pathname = isAdmin ? `/project/admin/extension/${key}` : `/project/extension/${key}`;
    return (
      <li key={key}>
        <Link to={{ pathname, query: { id: this.props.component.key } }} activeClassName="active">
          {name}
        </Link>
      </li>
    );
  };

  renderAdminExtensions() {
    const extensions = this.props.conf.extensions || [];
    return extensions.map(e => this.renderExtension(e, true));
  }

  renderExtensions() {
    const extensions = this.props.component.extensions || [];
    const withoutGovernance = extensions.filter(ext => ext.name !== 'Governance');
    if (!withoutGovernance.length) {
      return null;
    }

    return (
      <li className="dropdown">
        <a
          className="dropdown-toggle"
          id="component-navigation-more"
          data-toggle="dropdown"
          href="#">
          {translate('more')}&nbsp;
          <i className="icon-dropdown" />
        </a>
        <ul className="dropdown-menu">
          {withoutGovernance.map(e => this.renderExtension(e, false))}
        </ul>
      </li>
    );
  }

  render() {
    return (
      <NavBarTabs>
        {this.renderDashboardLink()}
        {this.renderIssuesLink()}
        {this.renderComponentMeasuresLink()}
        {this.renderCodeLink()}
        {this.renderActivityLink()}
        {this.renderAdministration()}
        {this.renderExtensions()}
      </NavBarTabs>
    );
  }
}
