import React from 'react';
import { Link } from 'react-router';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';
import moment from 'moment';
import Icon from '../Icon';

export default class VideoItem extends React.Component {
  renderPill() {
    switch (this.props.video.status) {
      case 'Expired':
        return <span className="publish__label label__expired">Expired</span>;
      case 'Active':
        return (
          <span className="publish__label label__live label__frontpage__overlay">
            Active
          </span>
        );
      case 'No Video':
        return (
          <span className="publish__label label__frontpage__novideo label__frontpage__overlay">
            No Video
          </span>
        );
      default:
        return '';
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
    const video = this.props.video;
    const scheduledLaunch = video.contentChangeDetails.scheduledLaunch && video.contentChangeDetails.scheduledLaunch.date;
    return (
      <li className="grid__item">
        <Link className="grid__link" to={'/videos/' + video.id}>

          <div className="grid__info">
            <div className="grid__image sixteen-by-nine">
              {this.renderItemImage()}
            </div>
            <div className="grid__status__overlay">
              {this.renderPill()}
              {
                scheduledLaunch ?
                  <span className="publish__label label__frontpage__scheduledLaunch label__frontpage__overlay">
                    <Icon textClass="--always-show" icon="access_time">{moment(scheduledLaunch).format('D MMM HH:mm')}</Icon>
                </span>
                : ''
              }
            </div>
            <div className="grid__item__footer">
              <span className="grid__item__title">
                {video.title}
              </span>
            </div>
          </div>
        </Link>
      </li>
    );
  }
}
