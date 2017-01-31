import React from 'react';
import {Link} from 'react-router';
import {getStore} from '../../util/storeAccessor';

export default class VideoAssetItem extends React.Component {

  youtubeThumbnailUrl = (assetId) => {
    const store = getStore();
    return store.getState().config.youtubeThumbnailUrl + assetId + "/0.jpg";
  };

  youtubeVideoUrl = (assetId) => {
    const store = getStore();
    return store.getState().config.youtubeEmbedUrl + assetId;
  };

  revertAsset = () => {
    this.props.revertAsset(this.props.asset.id, this.props.asset.version);
    // Update the activeAsset
    const newData = Object.assign({}, this.props.video, {
      activeVersion: this.props.asset.version
    });

    this.props.updateVideo(newData);
  };

  renderAssetVersionButton = () => {
    if(!this.props.activeAsset) {
      return (
        <button type="button" className="btn asset-list__makecurrent" onClick={this.revertAsset}>
          Set as current asset
        </button>
      )
    } else {
      return (
        <p className="asset-list__current">Current asset</p>
      );
    }
  };

  render() {
    return(
        <li className={"asset-list__item " + (this.props.activeAsset ? "asset-list__item--current" : false)}>
          <img className="asset-list__thumbnail" src={this.youtubeThumbnailUrl(this.props.asset.id)} />
          {this.renderAssetVersionButton()}
        </li>
    )
  }
}
