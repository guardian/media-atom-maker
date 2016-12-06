import React, {PropTypes} from 'react';
import {Link} from 'react-router';
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
        <div className="video-preview__video">No Active Video</div>
      )
    }

    if (activeAsset.platform !== "Youtube") {
      <div className="video-preview__video">Unable to Preview</div>
    }

    return  (
      <iframe className="video-preview__video" src={this.youtubeEmbedUrl() + activeAsset.id} allowFullScreen></iframe>
    )
  }

  render() {
    return (
        <div className="video-preview">
          <h2 className="video-preview__header">Preview</h2>
          {this.renderPreview()}
        </div>
    );
  }
}
