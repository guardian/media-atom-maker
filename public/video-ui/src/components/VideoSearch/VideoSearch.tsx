import React from 'react';
import Icon from '../Icon';
import type { Search } from '../../slices/search';

type Props = {
  saving: boolean;
  search: Search;
  updateSearchTerm: (term: string) => void;
};

export default class VideoSearch extends React.Component<Props> {
  onSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    this.props.updateSearchTerm(e.target.value);
  };

  searchInProgress = () => {
    return this.props.saving;
  };

  render() {
    return (
      <div className="topbar__search flex-container flex-grow">
        <Icon icon="search" />
        <input
          aria-label="search"
          className={
            'form__field' +
            (this.searchInProgress() ? ' form__field--loading' : '')
          }
          type="search"
          value={this.props.search.searchTerm || ''}
          onChange={this.onSearch}
          placeholder={'Search for videos...'}
        />
      </div>
    );
  }
}
