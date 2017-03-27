import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';
import Icon from '../../components/Icon';

export default class VideoSearch extends React.Component {

  onSearch = (e) => {
    this.props.updateSearchTerm(e.target.value);
  };

  searchInProgress = () => {
    return this.props.saving === saveStateVals.inprogress;
  };

  render () {
    return (
      <div className="topbar__search flex-container flex-grow">
        <Icon icon="search"/>
        <input className={'form__field' + (this.searchInProgress() ? ' form__field--loading' : '')} type="search" value={this.props.searchTerm || ''} onChange={this.onSearch} placeholder={"Search for videos..."} />
      </div>
    );
  }
}
