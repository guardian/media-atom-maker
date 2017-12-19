import React from 'react';
import { Link } from 'react-router';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';
import Icon from '../Icon';
import ReactTooltip from 'react-tooltip';
import VideoUtils from '../../util/video';
import { impossiblyDistantDate } from '../../constants/dates';

export default class VideoItem extends React.Component {
  renderPublishStatus() {
    if (VideoUtils.hasExpired(this.props.video)) {
      return (
        <span className="publish__label label__expired">Expired</span>
      );
    }

    if (VideoUtils.isPublished(this.props.video)) {
      return (
        <span className="publish__label label__live label__frontpage__overlay">
          Published
        </span>
      );
    } else {
      return (
        <span className="publish__label label__draft label__frontpage__overlay">
          Draft
        </span>
      )
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
    const scheduledLaunch = VideoUtils.getScheduledLaunch(video);
    const embargo = VideoUtils.getEmbargo(video);
    const hasPreventedPublication = embargo && embargo.valueOf() >= impossiblyDistantDate;
    return (
      <li className="grid__item">
        <Link className="grid__link" to={'/videos/' + video.id}>
          <div className="grid__info">
            <div className="grid__image sixteen-by-nine">
              {this.renderItemImage()}
            </div>
            <div className="grid__status__overlay">
              <ReactTooltip />
              {this.renderPublishStatus()}
              {embargo && (
                <span
                  data-tip={
                    hasPreventedPublication
                      ? 'This video has been embargoed indefinitely'
                      : `Embargoed until ${embargo.format(
                          'Do MMM YYYY HH:mm'
                        )}`
                  }
                  className="publish__label label__frontpage__embargo label__frontpage__overlay"
                >
                  <Icon textClass="always-show" icon="not_interested">
                    {hasPreventedPublication
                      ? 'Embargoed indefinitely'
                      : embargo.format('D MMM HH:mm')}
                  </Icon>
                </span>
              )}
              {scheduledLaunch && (
                <span
                  data-tip={`Scheduled to launch ${
                    scheduledLaunch.format('Do MMM YYYY HH:mm')}`}
                  className="publish__label label__frontpage__scheduledLaunch label__frontpage__overlay"
                >
                  <Icon textClass="always-show" icon="access_time">
                    {scheduledLaunch.format('D MMM HH:mm')}
                  </Icon>
                </span>
              )}
            </div>
            <div className="grid__item__footer">
              <span className="grid__item__title">{video.title}</span>
            </div>
          </div>
        </Link>
      </li>
    );
  }
}
