import React, {PropTypes} from 'react';
import {Link} from 'react-router';
import {getStore} from '../../util/storeAccessor';

export default class VideoPreview extends React.Component {

  getActiveAssetId = () => {
    for(let i=0; i < this.props.video.assets.length; i++) {
      if(this.props.video.activeVersion === this.props.video.assets[i].version) {
        return this.props.video.assets[i].id;
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
          <h2 className="video-preview__header">Preview</h2>
          <iframe className="video-preview__video" src={this.youtubeEmbedUrl() + this.getActiveAssetId()} allowfullscreen></iframe>
        </div>
    );
  }
}
