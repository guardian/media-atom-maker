import React from 'react';
import {getStore} from '../../util/storeAccessor';

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

  youtubeEmbedUrl = () => {
    const store = getStore();
    return store.getState().config.youtubeEmbedUrl;
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

    return  (
      <iframe className="baseline-margin" src={this.youtubeEmbedUrl() + activeAsset.id} allowFullScreen></iframe>
    );
  }

  render() {
    return (
        <div className="video-preview">
          {this.renderPreview()}
        </div>
    );
  }
}
