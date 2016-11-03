import React from 'react';
import {Link} from 'react-router';
import {getStore} from '../../util/storeAccessor';

export default class VideoAssetItem extends React.Component {

  youtubeEmbedUrl = () => {
    const store = getStore();
    return store.getState().config.youtubeEmbedUrl;
  };

  render() {

    return(
        <li className={"asset-list__item " + (this.props.activeAsset === this.props.asset.version ? "asset-list__item--active" : "")}>
          <iframe className="asset-list__video" src={this.youtubeEmbedUrl() + this.props.asset.id}></iframe>
          <p>Platform: {this.props.asset.platform}</p>
        </li>
    )
  }
}
