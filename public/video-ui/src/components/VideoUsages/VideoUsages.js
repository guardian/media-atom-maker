import React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import { getStore } from '../../util/storeAccessor';
import {
  FrontendIcon,
  ComposerIcon,
  ViewerIcon
} from '../Icon';

import ContentApi from '../../services/capi';

export default class VideoUsages extends React.Component {
  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  getViewerUrl = () => {
    return getStore().getState().config.viewerUrl;
  };

  renderUsage = ({ usage, state }) => {
    const composerLink = `${this.getComposerUrl()}/content/${usage.fields.internalComposerCode}`;
    const viewerLink = `${this.getViewerUrl()}/preview/${usage.id}`;
    const websiteLink = `https://www.theguardian.com/${usage.id}`;

    const usageDateFromNow = moment(usage.fields.creationDate).fromNow();

    return (
      <li key={usage.id} className="detail__list__item">
        <div className="details-list__title">

          {usage.webTitle || usage.id}
        </div>
        <div>
          Created:
          {' '}
          <span title={usage.fields.creationDate}>{usageDateFromNow}</span>
          <span className={`details-list__content-type details-list__content-type--${usage.type}`}>{usage.type}</span>
          <a
            className="usage--platform-link"
            href={composerLink}
            title="Open in Composer"
            target="_blank"
            rel="noopener noreferrer"
          >
            <ComposerIcon />
          </a>
          <a
            className="usage--platform-link"
            href={viewerLink}
            title="Open in Viewer"
            target="_blank"
            rel="noopener noreferrer"
          >
            <ViewerIcon />
          </a>

          {state === ContentApi.published
            ? <a className="usage--platform-link"
                 href={websiteLink}
                 title="Open on theguardian.com"
                 target="_blank"
                 rel="noopener noreferrer">
              <FrontendIcon />
            </a>
            : ''}
        </div>
      </li>
    );
  };

  renderUsages() {
    return Object.keys(this.props.usages).map(state => {
      const totalUsages = this.props.usages[state].video.length + this.props.usages[state].other.length;

      return (
        <div key={`${state}-usages`}>
          <h3>{state.charAt(0).toUpperCase() + state.slice(1)}</h3>
          {totalUsages === 0
            ? <div className="baseline-margin">{`No ${state} usages found`}</div>
            : <ul className="detail__list">
                {this.props.usages[state].video.map(usage => this.renderUsage({usage, state}))}
                {this.props.usages[state].other.map(usage => this.renderUsage({usage, state}))}
              </ul>}
        </div>
      );
    });
  }

  render() {
    return (
      <div className="form__group">
        {this.renderUsages()}
      </div>
    );
  }
}

VideoUsages.propTypes = {
  // usages: PropTypes.object.isRequired,
  video: PropTypes.object.isRequired,
  publishedVideo: PropTypes.object.isRequired
};
