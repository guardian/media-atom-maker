import React from 'react';
import { Video } from '../../services/VideosApi';
import { findAssetToUseAsThumbnail } from '../../util/imageHelpers';

type VideoSelectBarProps = {
  video: Video;
  onSelectVideo: () => void;
  embeddedMode: boolean;
};

export default class VideoSelectBar extends React.Component<VideoSelectBarProps> {
  renderItemImage() {
    if (!this.props.video) {
      return false;
    }

    const maybeThumbnailImage = this.props.video.posterImage
      ? findAssetToUseAsThumbnail(this.props.video.posterImage)
      : undefined;

    if (maybeThumbnailImage && maybeThumbnailImage.file) {
      return (
        <img src={maybeThumbnailImage.file} alt={this.props.video.title} />
      );
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

    const title =
      this.props.video && this.props.video.title
        ? this.props.video.title
        : 'Title missing';

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
