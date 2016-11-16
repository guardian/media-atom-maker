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
    this.props.revertAsset(this.props.asset.version);
  };

  renderAssetVersionButton = () => {
    if(this.props.activeAsset !== this.props.asset.version) {
      return (
        <button type="button" className="btn asset-list__makecurrent" onClick={this.revertAsset}>
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
          <img className="asset-list__thumbnail" src={this.youtubeThumbnailUrl(this.props.asset.id)}></img>
          <div className="asset-list__platform">
            {this.props.asset.platform} -
            <a target="_blank" href={this.youtubeVideoUrl(this.props.asset.id)}>{this.youtubeVideoUrl(this.props.asset.id)}</a>
          </div>
          {this.renderAssetVersionButton()}
        </li>
    )
  }
}
