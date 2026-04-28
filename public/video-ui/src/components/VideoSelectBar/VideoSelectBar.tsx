import React from 'react';
import { Video } from '../../services/VideosApi';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

type VideoSelectBarProps = {
  video: Video;
  onSelectVideo: () => void;
  /**
   * It looks like embeddedMode is actually an optional string, but being treated
   * here like a boolean. I don't want to mess with the business logic at the
   * moment so leaving this as an explicity `any` as a prompt to come back to
   * this when we hopefully have more time to do a more thorough refactor. 
   * */
  embeddedMode: any;
};

export default class VideoSelectBar extends React.Component<VideoSelectBarProps> {
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
      if (image && image.file) {
        return <img src={image.file} alt={this.props.video.title} />;
      }
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

    const title = this.props.video && this.props.video.title
      ? this.props.video.title
      : "Title missing";

    return (
      <div className="bar info-bar">
        <div className="bar__image">{this.renderItemImage()}</div>
        <div>
          <span className="grid__item__title">{title}</span>
          {this.renderEmbedButton()}
        </div>
      </div>
    );
  }
}
