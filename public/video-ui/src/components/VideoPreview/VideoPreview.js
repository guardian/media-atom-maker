import React, {PropTypes} from 'react';
import {Link} from 'react-router';
import {getStore} from '../../util/storeAccessor';

export default class VideoPreview extends React.Component {

  getActiveAssetId = () => {
    for(let i=0; i < this.props.video.data.assets.length; i++) {
      if(this.props.video.data.activeVersion === this.props.video.data.assets[i].version) {
        return this.props.video.data.assets[i].id;
      }
    }
  };

  youtubeEmbedUrl = () => {
    const store = getStore();
    return store.getState().config.youtubeEmbedUrl;
  };

  render() {
    return (
        <div className="video-preview">
          <h1 className="video-preview__header">Preview</h1>
          <iframe className="video-preview__video" src={this.youtubeEmbedUrl() + this.getActiveAssetId()}></iframe>
        </div>
    );
  }
}
