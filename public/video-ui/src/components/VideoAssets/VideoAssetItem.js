import React from 'react';
import {getStore} from '../../util/storeAccessor';
import Icon from '../Icon';

export default class VideoAssetItem extends React.Component {

  youtubeThumbnailUrl = (assetId) => {
    const store = getStore();
    return store.getState().config.youtubeThumbnailUrl + assetId + "/0.jpg";
  };

  youtubeVideoUrl = () => {
    const store = getStore();
    return store.getState().config.youtubeEmbedUrl + this.props.asset.id;
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
      );
    } else {
      return (
        <p className="asset-list__current">Current asset</p>
      );
    }
  };

  renderYoutubeLink = () => {
    return (
      <a href={this.youtubeVideoUrl()}
         title="Open on YouTube"
         target="_blank">
        <Icon icon="open_in_new"/>
      </a>
    );
  };

  render() {
    return(
        <li className={"asset-list__item " + (this.props.activeAsset ? "asset-list__item--current" : false)}>
          <img className="asset-list__thumbnail" src={this.youtubeThumbnailUrl(this.props.asset.id)} />
            {this.renderAssetVersionButton()}
            Video ID: {this.props.asset.id}
            {this.renderYoutubeLink()}
        </li>
    );
  }
}
