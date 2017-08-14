import React from 'react';
import { isVideoPublished } from '../../util/isVideoPublished';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

export default class VideoSelectBar extends React.Component {
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
          {this.renderEmbedButton()}
      </div>
    );
  }
}
