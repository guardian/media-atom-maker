import React from 'react';
import {Link} from 'react-router';

export default class VideoAssetItem extends React.Component {

  render() {
    console.log(this.props.activeAsset, this.props.asset.version);
    return(
        <li className={"asset-list__item " + (this.props.activeAsset === this.props.asset.version ? "asset-list__item--active" : "")}>
          <iframe className="asset-list__video" src={"https://www.youtube.com/embed/" + this.props.asset.id}></iframe>
          <p>Platform: {this.props.asset.platform}</p>
        </li>
    )
  }
}
