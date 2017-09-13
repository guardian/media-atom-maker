import React from 'react';
import PropTypes from 'prop-types';
import { VideoEmbed } from '../utils/VideoEmbed';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';

import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

export default class VideoPreview extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  renderPreview() {
    const activeVersion = this.props.video.activeVersion;
    const assets = this.props.video.assets || [];
    const active = assets.filter(asset => asset.version === activeVersion);

    if (active.length === 0) {
      return <div className="baseline-margin">No Active Video</div>;
    }

    if (active.length === 1 && active[0].platform === 'Youtube') {
      return <YouTubeEmbed id={active[0].id} className="video__preview__player" />;
    }

    const sources = active.map(asset => {
      return { src: asset.id, mimeType: asset.mimeType };
    });

    if (this.props.video.posterImage && this.props.video.posterImage.assets.length > 0) {
      const poster = findSmallestAssetAboveWidth(
        this.props.video.posterImage.assets
      );

      return <VideoEmbed sources={sources} posterUrl={poster.file}/>;
    }

    return <VideoEmbed sources={sources}/>;
  }

  render() {
    return (
      <div className="video__preview__container">
        {this.renderPreview()}
      </div>
    );
  }
}
