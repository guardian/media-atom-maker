import React from 'react'

export default class VideoSearch extends React.Component {

  onSearch = (e) => {
    this.props.updateSearchTerm(e.target.value);
  };

  render () {
    return (
      <form className="form topbar__search">
        <input className="form__field" type="search" value={this.props.searchTerm || ''} onChange={this.onSearch} placeholder={"Search for videos..."} />
      </form>
    )
  }
}
