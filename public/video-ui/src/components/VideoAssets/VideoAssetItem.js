import React from 'react';
import {Link} from 'react-router';
import {getStore} from '../../util/storeAccessor';

export default class VideoAssetItem extends React.Component {

  youtubeEmbedUrl = () => {
    const store = getStore();
    return store.getState().config.youtubeEmbedUrl;
  };

  revertAsset = () => {
    this.props.revertAsset(this.props.asset.version);

    // Update the activeAsset
    const newData = Object.assign({}, this.props.video.data, {
      activeVersion: this.props.asset.version
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  renderAssetVersionButton = () => {
    if(this.props.activeAsset !== this.props.asset.version) {
      return (
        <button type="button" className="btn" onClick={this.revertAsset}>
          Set as current asset
        </button>
      )
    } else {
      return false;
    }
  };

  render() {

    return(
        <li className={"asset-list__item " + (this.props.activeAsset === this.props.asset.version ? "asset-list__item--active" : "")}>
          <iframe className="asset-list__video" src={this.youtubeEmbedUrl() + this.props.asset.id}></iframe>
          <p>Platform: {this.props.asset.platform}</p>
          {this.renderAssetVersionButton()}
        </li>
    )
  }
}
