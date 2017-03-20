import React from 'react';
import YouTubeEmbed from '../utils/YouTubeEmbed';

export default class VideoPreview extends React.Component {

  getActiveAsset = () => {

    if (!this.props.video.assets || !this.props.video.activeVersion) {
      return false;
    }

    for(let i=0; i < this.props.video.assets.length; i++) {
      if(this.props.video.activeVersion === this.props.video.assets[i].version) {
        return this.props.video.assets[i];
      }
    }
  };

  renderPreview() {
    const activeAsset = this.getActiveAsset();

    if (!activeAsset) {
      return (
        <div className="baseline-margin">No Active Video</div>
      );
    }

    if (activeAsset.platform !== "Youtube") {
      <div className="baseline-margin">Unable to Preview</div>;
    }

    return <YouTubeEmbed id={activeAsset.id} />;
  }

  render() {
    return (
        <div className="video-preview">
          {this.renderPreview()}
        </div>
    );
  }
}
