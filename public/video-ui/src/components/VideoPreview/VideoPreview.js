import React, {PropTypes} from 'react';
import {Link} from 'react-router';

export default class VideoPreview extends React.Component {

  getActiveAssetId = () => {
    for(let i=0; i < this.props.video.data.assets.length; i++) {
      if(this.props.video.data.activeVersion === this.props.video.data.assets[i].version) {
        return this.props.video.data.assets[i].id;
      }
    }
  };

  render() {
    return (
        <div className="video-preview">
          <iframe className="asset-list__video" src={"https://www.youtube.com/embed/" + this.getActiveAssetId()}></iframe>
        </div>
    )
  }
}
