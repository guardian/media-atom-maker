import React from 'react';
import { isVideoPublished } from '../../util/isVideoPublished';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

export default class VideoSelectBar extends React.Component {
  renderItemImage() {
    if (!this.props.video) {
      return false;
    }

    if (this.props.video.posterImage) {
      const image = findSmallestAssetAboveWidth(
        this.props.video.posterImage.assets
      );
      return <img src={image.file} alt={this.props.video.title} />;
    }

    return <div className="bar__image-placeholder">No Image</div>;
  }

  renderEmbedButton() {
    // you can always select a video in 'preview' mode (this is used by the Pluto embed)
    // otherwise you should only be able to select published videos (when embedded inside Composer for example)

    const embedButton = (
      <button
        type="button"
        className="bar__button"
        onClick={this.props.onSelectVideo}
      >
        Select this Video
      </button>
    );

    switch (this.props.embeddedMode) {
      case 'preview':
        return embedButton;

      case 'live':
      case 'true':
        if (isVideoPublished(this.props.publishedVideo)) {
          return embedButton;
        } else {
          return (
            <div>
              This atom cannot be embedded because it has not been published
            </div>
          );
        }

      default:
        return false;
    }
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
