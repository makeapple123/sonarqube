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
import * as Select from 'react-select';
import { debounce } from 'lodash';
import { translate, translateWithParameters } from '../../helpers/l10n';

type Option = { label: string; value: string };

interface Props {
  autofocus?: boolean;
  minimumQueryLength?: number;
  onSearch: (query: string) => Promise<Array<Option>>;
  onSelect: (value: string) => void;
  renderOption?: (option: Object) => JSX.Element;
  resetOnBlur?: boolean;
  value?: string;
}

interface State {
  loading: boolean;
  options: Array<Option>;
  query: string;
}

export default class SearchSelect extends React.PureComponent<Props, State> {
  mounted: boolean;

  static defaultProps = {
    autofocus: true,
    minimumQueryLength: 2,
    resetOnBlur: true
  };

  constructor(props: Props) {
    super(props);
    this.state = { loading: false, options: [], query: '' };
    this.search = debounce(this.search, 250);
  }

  componentDidMount() {
    this.mounted = true;
  }

  componentWillUnmount() {
    this.mounted = false;
  }

  search = (query: string) => {
    this.props.onSearch(query).then(options => {
      if (this.mounted) {
        this.setState({ loading: false, options });
      }
    });
  };

  handleBlur = () => {
    this.setState({ options: [], query: '' });
  };

  handleChange = (option: Option) => {
    this.props.onSelect(option.value);
  };

  handleInputChange = (query: string = '') => {
    if (query.length >= (this.props.minimumQueryLength as number)) {
      this.setState({ loading: true, query });
      this.search(query);
    } else {
      this.setState({ options: [], query });
    }
  };

  // disable internal filtering
  handleFilterOption = () => true;

  render() {
    return (
      <Select
        autofocus={this.props.autofocus}
        className="input-super-large"
        clearable={false}
        filterOption={this.handleFilterOption as any}
        isLoading={this.state.loading}
        noResultsText={
          this.state.query.length < (this.props.minimumQueryLength as number)
            ? translateWithParameters('select2.tooShort', this.props.minimumQueryLength)
            : translate('select2.noMatches')
        }
        onBlur={this.props.resetOnBlur ? this.handleBlur : undefined}
        onChange={this.handleChange}
        onInputChange={this.handleInputChange}
        onOpen={this.props.minimumQueryLength === 0 ? this.handleInputChange : undefined}
        optionRenderer={this.props.renderOption}
        options={this.state.options}
        placeholder={translate('search_verb')}
        searchable={true}
        value={this.props.value}
        valueRenderer={this.props.renderOption}
      />
    );
  }
}
