import React from 'react';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

export default class VideoSelectBar extends React.Component {
  renderItemImage() {
    if (!this.props.video) {
      return false;
    }

    if (this.props.video.posterImage &&
        this.props.video.posterImage.assets &&
        this.props.video.posterImage.assets.length > 0) {
      const image = findSmallestAssetAboveWidth(
        this.props.video.posterImage.assets
      );
      return <img src={image.file} alt={this.props.video.title} />;
    }

    return <div className="bar__image-placeholder">No Image</div>;
  }

  renderEmbedButton() {

    return (
      <button
        type="button"
        className="bar__button"
        onClick={this.props.onSelectVideo}
      >
        Select this Video
      </button>
    );
  }

  render() {
    if (!this.props.embeddedMode) {
      return false;
    }

    return (
      <div className="bar info-bar">
        <div className="bar__image">{this.renderItemImage()}</div>
        <div>
          <span className="grid__item__title">{this.props.video.title}</span>
          {this.renderEmbedButton()}
        </div>
      </div>
    );
  }
}
