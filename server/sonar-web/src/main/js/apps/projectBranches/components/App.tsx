/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
import BranchRow from './BranchRow';
import { Branch } from '../../../app/types';
import { sortBranchesAsTree } from '../../../helpers/branches';
import { translate } from '../../../helpers/l10n';

interface Props {
  branches: Branch[];
  component: { key: string };
  onBranchesChange: () => void;
}

export default function App({ branches, component, onBranchesChange }: Props) {
  return (
    <div className="page page-limited">
      <header className="page-header">
        <h1 className="page-title">{translate('project_branches.page')}</h1>
      </header>

      <table className="data zebra zebra-hover">
        <thead>
          <tr>
            <th>{translate('branch')}</th>
            <th className="text-right">{translate('status')}</th>
            <th className="text-right">{translate('actions')}</th>
          </tr>
        </thead>
        <tbody>
          {sortBranchesAsTree(branches).map(branch => (
            <BranchRow
              branch={branch}
              component={component.key}
              key={branch.name}
              onChange={onBranchesChange}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
}
