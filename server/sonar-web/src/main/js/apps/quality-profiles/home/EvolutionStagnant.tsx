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
import DateFormatter from '../../../components/intl/DateFormatter';
import ProfileLink from '../components/ProfileLink';
import { translate, translateWithParameters } from '../../../helpers/l10n';
import { isStagnant } from '../utils';
import { Profile } from '../types';

interface Props {
  organization: string | null;
  profiles: Profile[];
}

export default function EvolutionStagnant(props: Props) {
  // TODO filter built-in out

  const outdated = props.profiles.filter(isStagnant);

  if (outdated.length === 0) {
    return null;
  }

  return (
    <div className="quality-profile-box quality-profiles-evolution-stagnant">
      <div className="spacer-bottom">
        <strong>{translate('quality_profiles.stagnant_profiles')}</strong>
      </div>
      <div className="spacer-bottom">
        {translate('quality_profiles.not_updated_more_than_year')}
      </div>
      <ul>
        {outdated.map(profile => (
          <li key={profile.key} className="spacer-top">
            <div className="text-ellipsis">
              <ProfileLink
                className="link-no-underline"
                language={profile.language}
                name={profile.name}
                organization={props.organization}>
                {profile.name}
              </ProfileLink>
            </div>
            {profile.rulesUpdatedAt && (
              <DateFormatter date={profile.rulesUpdatedAt} long={true}>
                {formattedDate => (
                  <div className="note">
                    {translateWithParameters(
                      'quality_profiles.x_updated_on_y',
                      profile.languageName,
                      formattedDate
                    )}
                  </div>
                )}
              </DateFormatter>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}
