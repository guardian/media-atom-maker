import React from 'react';

export default class ContentFlags extends React.Component {

  renderLegallySensitiveCheckbox() {
    return (
      <div>
        <input
          id="legallySensitive"
          type="checkbox"
          disabled={!this.props.editable}
          checked={this.props.video.legallySensitive || false}
          onChange={this.updateFlag.bind(this)}
        />
      </div>
    );
  }

  updateFlag(e) {
    e.preventDefault();
    const flag = {[e.target.id]: e.target.checked};
    const video = Object.assign({}, this.props.video, flag);
    this.props.saveAndUpdateVideo(video);
  }

  render(){
    return(
      <div>
        <p className="details-list__title">Legally sensitive</p>
        <div className="details-list__labeled-filter">
          {this.renderLegallySensitiveCheckbox()}
          <p className="details-list__field details-list__labeled-filter__label">This content involves active criminal proceedings.</p>
        </div>
      </div>
    );
  }

}
