import React from 'react';
import { Link } from 'react-router';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

export default class VideoItem extends React.Component {
  renderPill() {
    switch (this.props.video.status) {
      case 'Expired':
        return <div className="publish__label label__expired">Expired</div>;
      case 'Active':
        return (
          <span className="publish__label label__live label__frontpage__overlay">
            Active
          </span>
        );
      default:
        return (
          <span className="publish__label label__frontpage__novideo label__frontpage__overlay">
            No Video
          </span>
        );
    }
  }

  renderItemImage() {
    if (this.props.video.posterImage) {
      const image = findSmallestAssetAboveWidth(
        this.props.video.posterImage.assets
      );

      return <img src={image.file} alt={this.props.video.title} />;
    }

    return <div className="grid__image__placeholder">No Image</div>;
  }

  render() {
    return (
      <li className="grid__item">
        <Link className="grid__link" to={'/videos/' + this.props.video.id}>

          <div className="grid__info">
            <div className="grid__image sixteen-by-nine">
              {this.renderItemImage()}
            </div>
            <div className="grid__status__overlay">
              {this.renderPill()}
            </div>
            <div className="grid__item__footer">
              <span className="grid__item__title">
                {this.props.video.title}
              </span>
            </div>
          </div>
        </Link>
      </li>
    );
  }
}
