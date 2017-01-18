import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';

export default class VideoSelectBar extends React.Component {

  renderItemImage() {
    if (!this.props.video) {
      return  false;
    }

    if (this.props.video.posterImage) {
      return  <img src={this.props.video.posterImage.master.file} alt={this.props.video.title}/>
    }

    return <div className="bar__image-placeholder">No Image</div>
  }

  isVideoPublished() {
    return this.props.video && this.props.video.contentChangeDetails && this.props.video.contentChangeDetails.published;

  }

  render() {
    if (!this.props.embeddedMode) {
       return false;
    }

    if (this.isVideoPublished()) {
      return (
        <div className="bar info-bar">
        <div className="bar__image">{this.renderItemImage()}</div>
        <div>
          <span className="grid__item__title">{this.props.video.title}</span>
          <button type="button" className="bar__button" onClick={this.props.onSelectVideo}>Select this Video</button>
        </div>
        </div>
      )
    } else {
      return (
        <div className="bar info-bar">
        <div className="bar__image">{this.renderItemImage()}</div>
        <div>
          <span className="grid__item__title">{this.props.video.title}</span>
          <div>This atom has not been embedded because it has not been published</div>
        </div>
        </div>
      )
    }
  }
}
