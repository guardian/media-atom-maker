import React from 'react'

export default class VideoSearch extends React.Component {

  constructor(props) {
    super(props)
  }

  onSearch = (e) => {
    console.log("Here");
    this.props.updateSearchTerm(e.target.value);
  };

  render () {
    return (
      <form className="form">
        <div className="form__row">
          <input className="form__field" type="text" value={this.props.searchTerm || ''} onChange={this.onSearch} />
        </div>
      </form>
    )
  }
}
