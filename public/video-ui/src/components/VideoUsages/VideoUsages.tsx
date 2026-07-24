import React from 'react';
import moment from 'moment';
import { getStore } from '../../util/storeAccessor';
import { FrontendIcon, ComposerIcon, ViewerIcon } from '../Icon';

import ContentApi from '../../services/capi';
import { UsageState } from '../../slices/usage';
import { Video } from '../../services/VideosApi';

type Props = {
  usages: UsageState;
  video: Video;
  publishedVideo: Video;
};

export default class VideoUsages extends React.Component<Props> {
  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  getViewerUrl = () => {
    return getStore().getState().config.viewerUrl;
  };

  renderUsage = ({ usage, state }: any) => {
    const composerLink = `${this.getComposerUrl()}/content/${usage.fields.internalComposerCode}`;
    const viewerLink = `${this.getViewerUrl()}/preview/${usage.id}`;
    const websiteLink = `https://www.theguardian.com/${usage.id}`;

    const usageDateFromNow = moment(usage.fields.creationDate).fromNow();

    return (
      <li key={usage.id} className="detail__list__item">
        <div className="details-list__title">
          <span
            className={`usage__content-type usage__content-type--${usage.type}`}
          >
            {usage.type}
          </span>
          {usage.webTitle || usage.id}
        </div>
        <div>
          Created:{' '}
          <span title={usage.fields.creationDate}>{usageDateFromNow}</span>
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
          {state === ContentApi.published ? (
            <a
              className="usage--platform-link"
              href={websiteLink}
              title="Open on theguardian.com"
              target="_blank"
              rel="noopener noreferrer"
            >
              <FrontendIcon />
            </a>
          ) : (
            ''
          )}
        </div>
      </li>
    );
  };

  renderUsages() {
    const usages = this.props.usages.data;

    return Object.keys(usages).map(state => {
      const totalUsages =
        // @ts-expect-error TS(7053): Element implicitly has an 'any' type because expre... Remove this comment to see the full error message
        usages[state].video.length + usages[state].other.length;

      return (
        <div key={`${state}-usages`}>
          <p className="details-list__title">{`${state.charAt(0).toUpperCase() + state.slice(1)} (Total: ${totalUsages})`}</p>
          {totalUsages === 0 ? (
            <p className="usage--none details-list__field">{`No ${state} usages found`}</p>
          ) : (
            <ul className="detail__list">
              {/* @ts-expect-error TS(7053): Element implicitly has an 'any' type because expre... Remove this comment to see the full error message */}
              {usages[state].video.map((usage: any) =>
                this.renderUsage({ usage, state })
              )}
              {/* @ts-expect-error TS(7053): Element implicitly has an 'any' type because expre... Remove this comment to see the full error message */}
              {usages[state].other.map((usage: any) =>
                this.renderUsage({ usage, state })
              )}
            </ul>
          )}
        </div>
      );
    });
  }

  render() {
    return <div className="usage">{this.renderUsages()}</div>;
  }
}
