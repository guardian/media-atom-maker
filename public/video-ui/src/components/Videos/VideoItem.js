import React from 'react';
import {Link} from 'react-router';

export default class VideoItem extends React.Component {

  renderActiveAssetName() {

    if (this.props.video.activeVersion && this.props.video.assets.length) {
      const activeAssets = this.props.video.assets.filter((asset) => asset.version === this.props.video.activeVersion)
      if (!activeAssets.length) {
        if (activeAssets[0] && activeAssets[0].platform) {
          return <span className="success">Active {activeAssets[0].platform} video</span>
        }
      }
    }

    return <span className="error">No Active Assets</span>
  }

  renderItemImage() {
    if (this.props.video.posterImage) {
      return  <img src={this.props.video.posterImage.master.file} alt={this.props.video.title}/>
    }

    return <div className="grid__image__placeholder">No Image</div>
  }

  render() {
    return(
        <li className="grid__item">
          <Link className="grid__link" to={'/video/videos/' + this.props.video.id}>

            <div className="grid__info">
              <div className="grid__image">
                {this.renderItemImage()}
              </div>
              <div className="grid__item__footer">
                <span className="grid__item__title">{this.props.video.title}</span>
                {this.renderActiveAssetName()}
              </div>
            </div>
          </Link>
        </li>
    )
  }
}
