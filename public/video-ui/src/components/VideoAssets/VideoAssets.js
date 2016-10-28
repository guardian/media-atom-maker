import React, {PropTypes} from 'react';
import {Link} from 'react-router';
import VideoAssetItem from './VideoAssetItem';

export default class VideoAssets extends React.Component {

  renderList() {
      if(this.props.video.data.assets) {
        return (
          <ul className="asset-list">
            {this.renderListItems()}
          </ul>
        )
      } else {
        return (<p>No assets found</p>)
      }
  }

  renderListItems() {
    return (
        this.props.video.data.assets.map((asset, index) => <VideoAssetItem key={index} asset={asset} activeAsset={this.props.video.data.activeVersion}/>)
    );
  }


  render() {
    return (
        <div className="video__sidebar video-assets">
          {this.renderList()}
        </div>
    )
  }
}
