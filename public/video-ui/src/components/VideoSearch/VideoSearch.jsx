import React from 'react';
import Icon from '../Icon';

export default class VideoSearch extends React.Component {
  onSearch = e => {
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
          value={this.props.searchTerm || ''}
          onChange={this.onSearch}
          placeholder={'Search for videos...'}
        />
      </div>
    );
  }
}
