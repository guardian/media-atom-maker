import React from 'react';
import { VideoEmbed } from '../utils/VideoEmbed';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';

export default class VideoPreview extends React.Component {
  renderPreview() {
    const { assets, activeVersion } = this.props.video;
    const relevant = assets.filter(asset => asset.version === activeVersion);

    if (!assets || !activeVersion || relevant.length === 0) {
      return <div className="baseline-margin">No Active Video</div>;
    }

    if (relevant.length === 1 && relevant[0].id) {
      return <YouTubeEmbed id={relevant[0].id} className="baseline-margin" />;
    }

    const sources = relevant.map(asset => {
      return { src: asset.id, mimeType: asset.mimeType };
    });

    return <VideoEmbed sources={sources} />;
  }

  render() {
    return (
      <div className="video-preview">
        {this.renderPreview()}
      </div>
    );
  }
}
