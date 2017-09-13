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
import React from 'react';
import classNames from 'classnames';
import { IndexLink, Link } from 'react-router';
import { connect } from 'react-redux';
import ContextNavBar from '../../../../components/nav/ContextNavBar';
import NavBarTabs from '../../../../components/nav/NavBarTabs';
import { translate } from '../../../../helpers/l10n';
import { areThereCustomOrganizations } from '../../../../store/rootReducer';

class SettingsNav extends React.PureComponent {
  static defaultProps = {
    extensions: []
  };

  isSomethingActive(urls) {
    const path = window.location.pathname;
    return urls.some(url => path.indexOf(window.baseUrl + url) === 0);
  }

  isSecurityActive() {
    const urls = [
      '/admin/users',
      '/admin/groups',
      '/admin/permissions',
      '/admin/permission_templates'
    ];
    return this.isSomethingActive(urls);
  }

  isProjectsActive() {
    const urls = ['/admin/projects_management', '/admin/background_tasks'];
    return this.isSomethingActive(urls);
  }

  isSystemActive() {
    const urls = ['/admin/update_center', '/admin/system'];
    return this.isSomethingActive(urls);
  }

  renderExtension = ({ key, name }) => {
    return (
      <li key={key}>
        <Link to={`/admin/extension/${key}`} activeClassName="active">
          {name}
        </Link>
      </li>
    );
  };

  render() {
    const isSecurity = this.isSecurityActive();
    const isProjects = this.isProjectsActive();
    const isSystem = this.isSystemActive();

    const securityClassName = classNames('dropdown-toggle', { active: isSecurity });
    const projectsClassName = classNames('dropdown-toggle', { active: isProjects });
    const systemClassName = classNames('dropdown-toggle', { active: isSystem });
    const configurationClassNames = classNames('dropdown-toggle', {
      active: !isSecurity && !isProjects && !isSystem
    });

    return (
      <ContextNavBar id="context-navigation" height={65}>
        <h1 className="navbar-context-header">
          <strong>{translate('layout.settings')}</strong>
        </h1>

        <NavBarTabs>
          <li className="dropdown">
            <a
              className={configurationClassNames}
              data-toggle="dropdown"
              id="settings-navigation-configuration"
              href="#">
              {translate('sidebar.project_settings')} <i className="icon-dropdown" />
            </a>
            <ul className="dropdown-menu">
              <li>
                <IndexLink to="/admin/settings" activeClassName="active">
                  {translate('settings.page')}
                </IndexLink>
              </li>
              <li>
                <IndexLink to="/admin/settings/licenses" activeClassName="active">
                  {translate('property.category.licenses')}
                </IndexLink>
              </li>
              <li>
                <IndexLink to="/admin/settings/encryption" activeClassName="active">
                  {translate('property.category.security.encryption')}
                </IndexLink>
              </li>
              <li>
                <IndexLink to="/admin/settings/server_id" activeClassName="active">
                  {translate('property.category.server_id')}
                </IndexLink>
              </li>
              <li>
                <IndexLink to="/admin/custom_metrics" activeClassName="active">
                  Custom Metrics
                </IndexLink>
              </li>
              {this.props.extensions.map(this.renderExtension)}
            </ul>
          </li>

          <li className="dropdown">
            <a className={securityClassName} data-toggle="dropdown" href="#">
              {translate('sidebar.security')} <i className="icon-dropdown" />
            </a>
            <ul className="dropdown-menu">
              <li>
                <IndexLink to="/admin/users" activeClassName="active">
                  {translate('users.page')}
                </IndexLink>
              </li>
              {!this.props.customOrganizations && (
                <li>
                  <IndexLink to="/admin/groups" activeClassName="active">
                    {translate('user_groups.page')}
                  </IndexLink>
                </li>
              )}
              {!this.props.customOrganizations && (
                <li>
                  <IndexLink to="/admin/permissions" activeClassName="active">
                    {translate('global_permissions.page')}
                  </IndexLink>
                </li>
              )}
              {!this.props.customOrganizations && (
                <li>
                  <IndexLink to="/admin/permission_templates" activeClassName="active">
                    {translate('permission_templates')}
                  </IndexLink>
                </li>
              )}
            </ul>
          </li>

          <li className="dropdown">
            <a className={projectsClassName} data-toggle="dropdown" href="#">
              {translate('sidebar.projects')} <i className="icon-dropdown" />
            </a>
            <ul className="dropdown-menu">
              {!this.props.customOrganizations && (
                <li>
                  <IndexLink to="/admin/projects_management" activeClassName="active">
                    Management
                  </IndexLink>
                </li>
              )}
              <li>
                <IndexLink to="/admin/background_tasks" activeClassName="active">
                  {translate('background_tasks.page')}
                </IndexLink>
              </li>
            </ul>
          </li>

          <li className="dropdown">
            <a className={systemClassName} data-toggle="dropdown" href="#">
              {translate('sidebar.system')} <i className="icon-dropdown" />
            </a>
            <ul className="dropdown-menu">
              <li>
                <IndexLink to="/admin/update_center" activeClassName="active">
                  {translate('update_center.page')}
                </IndexLink>
              </li>
              <li>
                <IndexLink to="/admin/system" activeClassName="active">
                  {translate('system_info.page')}
                </IndexLink>
              </li>
            </ul>
          </li>
        </NavBarTabs>
      </ContextNavBar>
    );
  }
}

const mapStateToProps = state => ({
  customOrganizations: areThereCustomOrganizations(state)
});

export default connect(mapStateToProps)(SettingsNav);

export const UnconnectedSettingsNav = SettingsNav;
