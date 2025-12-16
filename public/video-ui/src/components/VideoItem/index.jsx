import React from 'react';
import { Link } from 'react-router';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';
import Icon from '../Icon';
import ReactTooltip from 'react-tooltip';
import VideoUtils from '../../util/video';
import { impossiblyDistantDate } from '../../constants/dates';
import moment from 'moment';
import Youtube from "../../../images/youtube.svg?react";
import Loop from "../../../images/loop.svg?react";
import Cinemagraph from "../../../images/cinemagraph.svg?react";
import Standard from "../../../images/standard.svg?react";


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
    const video = this.props.video;
    const mediaPlatform = VideoUtils.getMediaPlatform(video);
    const scheduledLaunch = VideoUtils.getScheduledLaunch(video);
    const scheduledLaunchMoment = moment(scheduledLaunch);
    const embargo = VideoUtils.getEmbargo(video);
    const embargoMoment = moment(embargo);
    const hasPreventedPublication = embargo && embargoMoment.valueOf() >= impossiblyDistantDate;
    const iconMap = {
      Youtube: <Youtube/>,
      Loop: <Loop/>,
      Cinemagraph: <Cinemagraph/>,
      Default: <Standard/>
    };

    return (
      <li className="grid__item">
        <div className="presence-section presence-section-front">
          <ul className="presence-list presence-list-front">
            {this.props.presences.map(presence => {
              const id = presence.clientId.connId;
              const { firstName, lastName } = presence.clientId.person;
              const initials = `${firstName.slice(0, 1)}${lastName.slice(0, 1)}`;
              const fullName = `${firstName} ${lastName}`;

              return (
                <li key={id} className="presence-list__user presence-list-front__user" title={fullName}>
                  {initials}
                </li>
              );
            })}
          </ul>
        </div>
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
                      : `Embargoed until ${embargoMoment.format(
                          'Do MMM YYYY HH:mm'
                        )}`
                  }
                  className="publish__label label__frontpage__embargo label__frontpage__overlay"
                >
                  <Icon textClass="always-show" icon="not_interested">
                    {hasPreventedPublication
                      ? 'Embargoed indefinitely'
                      : embargoMoment.format('D MMM HH:mm')}
                  </Icon>
                </span>
              )}
              {scheduledLaunch && (
                <span
                  data-tip={`Scheduled to launch ${
                    scheduledLaunchMoment.format('Do MMM YYYY HH:mm')}`}
                  className="publish__label label__frontpage__scheduledLaunch label__frontpage__overlay"
                >
                  <Icon textClass="always-show" icon="access_time">
                    {scheduledLaunchMoment.format('D MMM HH:mm')}
                  </Icon>
                </span>
              )}
            </div>
            <div className="platform__icons">
              <div className="platform__icon">
                  {
                    video.videoPlayerFormat ?
                      iconMap[video.videoPlayerFormat] :
                      iconMap[mediaPlatform?.toLowerCase() === 'youtube' ? 'Youtube' : 'Default']
                  }
              </div>
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
