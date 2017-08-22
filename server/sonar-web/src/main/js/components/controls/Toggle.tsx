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
import './styles.css';

interface Props {
  name?: string;
  onChange?: (value: boolean) => void;
  value: boolean | string;
}

export default class Toggle extends React.PureComponent<Props> {
  getValue = (): boolean => {
    const { value } = this.props;
    return typeof value === 'string' ? value === 'true' : value;
  };

  handleClick = (event: React.SyntheticEvent<HTMLButtonElement>) => {
    event.preventDefault();
    event.currentTarget.blur();
    if (this.props.onChange) {
      this.props.onChange(!this.getValue());
    }
  };

  render() {
    const className = classNames('boolean-toggle', { 'boolean-toggle-on': this.getValue() });

    return (
      <button className={className} name={this.props.name} onClick={this.handleClick}>
        <div className="boolean-toggle-handle" />
      </button>
    );
  }
}
