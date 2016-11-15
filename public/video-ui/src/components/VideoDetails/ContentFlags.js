import React, {PropTypes} from 'react';

export default class ContentFlags extends React.Component {

  renderLegallySensitiveButton() {
    const classes = this.props.video.legallySensitive ? "on-btn on-switch" : "flag-btn off-switch";
    const text = this.props.video.legallySensitive ? "On" : "Off";
    const value = !this.props.video.legallySensitive;
    return <button name="legallySensitive" value={value} className={classes} onClick={this.updateFlag.bind(this)}>{text}</button>
  }

  updateFlag(e) {
    const flag = {[e.target.name]: e.target.value}
    const video = Object.assign({}, this.props.video, flag);
    this.props.updateVideoFlags(video)
  }

  render(){
    return(
      <div>
      <p className="details-list__title">Filters</p>
        <table className="details-list">
          <tbody className="details-list__flags">
            <tr>
              <td colSpan="2" className="details-list__title">Legally sensitive</td>
            </tr>
            <tr>
              <td>{this.renderLegallySensitiveButton()}</td>
              <td className="details-list__field">This content involves active criminal proceedings.</td>
            </tr>
          </tbody>
        </table>

      </div>
    )
  }

}
