import React from 'react';
import { VideoEmbed } from '../utils/VideoEmbed';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';

export default class VideoPreview extends React.Component {
  renderPreview() {
    const activeVersion = this.props.video.activeVersion;
    const assets = this.props.video.assets || [];
    const active = assets.filter(asset => asset.version === activeVersion);

    if (active.length === 0) {
      return <div>No Active Video</div>;
    }

    if (active.length === 1 && active[0].platform === 'Youtube') {
      return <YouTubeEmbed id={active[0].id} />;
    }

    const sources = active.map(asset => {
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
