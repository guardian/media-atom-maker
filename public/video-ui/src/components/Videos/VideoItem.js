import React from 'react';
import {Link} from 'react-router';
import {findSmallestAssetAboveWidth} from '../../util/imageHelpers';

export default class VideoItem extends React.Component {

  renderActiveAssetName() {
    const activeVersion = this.props.video.activeVersion ? this.props.video.activeVersion : 0;

    if(activeVersion === -1) {
      // search results do not contain the version
      return "";
    } else {
      const assets = this.props.video.assets.filter((asset) => asset.version === activeVersion);

      if(assets.length && assets[0] && assets[0].platform) {
        return <span className="publish__label label__live label__overlay">Live</span>;
      } else {
        return <span className="publish__label label__draft label__overlay">Draft</span>;
      }
    }
  }

  renderItemImage() {
    if (this.props.video.posterImage) {
      const image = findSmallestAssetAboveWidth(this.props.video.posterImage.assets);

      return <img src={image.file} alt={this.props.video.title}/>;
    }

    return <div className="grid__image__placeholder">No Image</div>;
  }

  render() {
    return(
        <li className="grid__item">
          <Link className="grid__link" to={'/videos/' + this.props.video.id}>

            <div className="grid__info">
              <div className="grid__image">
                {this.renderItemImage()}
                {this.renderActiveAssetName()}
              </div>
              <div className="grid__item__footer">
                <span className="grid__item__title">{this.props.video.title}</span>
              </div>
            </div>
          </Link>
        </li>
    );
  }
}
