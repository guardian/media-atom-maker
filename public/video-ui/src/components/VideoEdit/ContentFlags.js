import React from 'react';

export default class ContentFlags extends React.Component {

  renderLegallySensitiveCheckbox() {
    return (
      <div className="form-checkbox" >
        <input
          id="legallySensitive"
          className="form-checkbox__input"
          type="checkbox"
          checked={this.props.video.legallySensitive}
          onChange={this.updateFlag.bind(this)}
        />
        <label htmlFor="legallySensitive" className="form-checkbox__toggle"></label>
      </div>
    )
  }

  updateFlag(e) {
    e.preventDefault();
    const flag = {[e.target.id]: e.target.checked}
    const video = Object.assign({}, this.props.video, flag);
    this.props.saveAndUpdateVideo(video)
  }

  render(){
    return(
      <div>
        <p className="details-list__title">Filters</p>
        <p className="details-list__title">Legally sensitive</p>
        <div className="details-list__labeled-filter">
          {this.renderLegallySensitiveCheckbox()}
          <p className="details-list__field details-list__labeled-filter__label">This content involves active criminal proceedings.</p>
        </div>
      </div>
    )
  }

}
