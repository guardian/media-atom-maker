import React, {PropTypes} from 'react';

export default class ContentFlags extends React.Component {

  renderLegallySensitive() {
    const classes = this.props.video.data.legallySensitive ? "on-btn on-switch" : "flag-btn off-switch";
    const text = this.props.video.data.legallySensitive ? "On" : "Off";
    const value = !!this.props.video.data.legallySensitive;
    return <button name="legallySensitive" value={value} className={classes} onClick={this.updateFlag.bind(this)}>{text}</button>
  }

  renderSensitive() {
    const classes = this.props.video.data.sensitive ? "on-btn on-switch" : "flag-btn off-switch";
    const text = this.props.video.data.sensitive ? "On" : "Off";
    const value = !!this.props.video.data.sensitive;
    return <button name="sensitive" value={value} className={classes} onClick={this.updateFlag.bind(this)}>{text}</button>
  }

  renderSuppressRelatedContent() {
    const classes = this.props.video.data.suppressRelatedContent ? "on-btn on-switch" : "flag-btn off-switch";
    const text = this.props.video.data.suppressRelatedContent ? "On" : "Off";
    const value = !!this.props.video.data.suppressRelatedContent;
    return <button name="suppressRelatedContent" value={value} className={classes} onClick={this.updateFlag.bind(this)}>{text}</button>
  }

  renderBlockAds() {
    const classes = this.props.video.data.blockAds ? "on-btn on-switch" : "flag-btn off-switch";
    const text = this.props.video.data.blockAds ? "On" : "Off";
    const value = !!this.props.video.data.blockAds;
    return <button name="blockAds" value={value} className={classes} onClick={this.updateFlag.bind(this)}>{text}</button>
  }

  updateFlag(e) {
    const flagName = e.target.name;
    const flagValue = e.target.value;
    const data = Object.assign({}, this.props.video.data, {
      settings: {[flagName]: flagValue}
    });
    const video = Object.assign({}, this.props.video, {
      data: data
    });
    console.log(video)
    this.props.updateVideoFlags(video)
  }

  render(){
    return(
      <div>
      <p className="details-list__title">Filters</p>
        <table className="details-list">
          <tbody>
            <tr>
              <td colSpan="2" className="details-list__title">Sensitive</td>
            </tr>
            <tr>
              <td>{this.renderSensitive()}</td>
              <td className="details-list__field">This content features children, vulnerable people, or is on a topic that is likely to attract online abuse. If unsure, see <a href="https://docs.google.com/document/d/1BvDwS3oEU5lR5ZB0Us6FlTzx31B6dtzrnD3JXgSpYfI/edit?ts=56b85913#" target="_blank">this document.</a></td>
            </tr>
            <tr>
              <td colSpan="2" className="details-list__title">Legally sensitive</td>
            </tr>
            <tr>
              <td>{this.renderLegallySensitive()}</td>
              <td className="details-list__field">This content involves active criminal proceedings.</td>
            </tr>
            <tr>
              <td colSpan="2" className="details-list__title">Suppress related content</td>
            </tr>
            <tr>
              <td>{this.renderSuppressRelatedContent()}</td>
              <td className="details-list__field">Hides related Guardian content and removes from related content results.</td>
            </tr>
            <tr>
              <td colSpan="2" className="details-list__title">Block ads</td>
            </tr>
            <tr>
              <td>{this.renderBlockAds()}</td>
              <td className="details-list__field">Hides internal modules and adverts.</td>
            </tr>
          </tbody>
        </table>

      </div>
    )
  }

}
