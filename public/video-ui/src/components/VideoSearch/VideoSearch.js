import React from 'react'
import {saveStateVals} from '../../constants/saveStateVals';

export default class VideoSearch extends React.Component {

  onSearch = (e) => {
    this.props.updateSearchTerm(e.target.value);
  };

  searchInProgress = () => {
    return this.props.searching === saveStateVals.inprogress;
  };

  render () {
    return (
      <div className="topbar__search">
        <i className="icon icon__search-magnifier">search</i>
        <input className={'form__field' + (this.searchInProgress() ? ' form__field--loading' : '')} type="search" value={this.props.searchTerm || ''} onChange={this.onSearch} placeholder={"Search for videos..."} />
      </div>
    )
  }
}
