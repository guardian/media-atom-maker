import React from 'react';
import {isVideoPublished} from '../../util/isVideoPublished';

export default class VideoSelectBar extends React.Component {

  renderItemImage() {
    if (!this.props.video) {
      return  false;
    }

    if (this.props.video.posterImage) {
      return  <img src={this.props.video.posterImage.master.file} alt={this.props.video.title}/>;
    }

    return <div className="bar__image-placeholder">No Image</div>;
  }

  renderEmbedButton() {
    return <button type="button" className="bar__button" onClick={this.props.onSelectVideo}>Select this Video</button>;
  }

  renderCannotEmbedMessage() {
    return <div>This atom cannot be embedded because it has not been published</div>;
  }

  render() {
    if (!this.props.embeddedMode) {
       return false;
    }

    if (isVideoPublished(this.props.publishedVideo)) {
      return (
        <div className="bar info-bar">
        <div className="bar__image">{this.renderItemImage()}</div>
        <div>
          <span className="grid__item__title">{this.props.video.title}</span>
          {this.renderEmbedButton()}
        </div>
        </div>
      );
    } else {
      return (
        <div className="bar info-bar">
        <div className="bar__image">{this.renderItemImage()}</div>
        <div>
          <span className="grid__item__title">{this.props.video.title}</span>
          {this.renderCannotEmbedMessage()}
        </div>
        </div>
      );
    }
  }
}
