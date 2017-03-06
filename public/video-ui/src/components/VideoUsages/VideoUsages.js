import React from 'react';
import moment from 'moment';
import {getVideoBlock} from '../../util/getVideoBlock';
import {getStore} from '../../util/storeAccessor';
import {hasUnpublishedChanges} from '../../util/hasUnpublishedChanges';
import {FrontendIcon, ComposerIcon, ViewerIcon} from '../Icon';

export default class VideoUsages extends React.Component {

  state = {
    pageCreated: false
  };

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  getViewerUrl = () => {
    return getStore().getState().config.viewerUrl;
  };

  pageCreate = () => {

    this.setState({
      pageCreated: true
    });

    const metadata = {
      title: this.props.video.title,
      standfirst: this.props.video.description
    };

    const videoBlock = getVideoBlock(this.props.video.id, metadata);

    return this.props.createComposerPage(this.props.video.id, metadata, this.getComposerUrl(), videoBlock);
  };

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(this.props.video, this.props.publishedVideo);
  }

  renderUsage = (usage) => {
    const composerLink = `${this.getComposerUrl()}/content/${usage.fields.internalComposerCode}`;
    const viewerLink = `${this.getViewerUrl()}/preview/${usage.id}`;
    const websiteLink = `https://www.theguardian.com/${usage.id}`;

    const usageDateFromNow = moment(usage.fields.creationDate).fromNow();

    //TODO add an icon to indicate atom usage on a video page
    return (
      <li key={usage.id} className="detail__list__item">
        {usage.fields.headline || usage.id}
        <div>
          Created: <span title={usage.fields.creationDate}>{usageDateFromNow}</span>
          <a className="usage--platform-link" href={websiteLink} title="Open on theguardian.com" target="_blank" rel="noopener noreferrer">
            <FrontendIcon />
          </a>
          <a className="usage--platform-link" href={composerLink} title="Open in Composer" target="_blank" rel="noopener noreferrer">
            <ComposerIcon />
          </a>
          <a className="usage--platform-link" href={viewerLink} title="Open in Viewer" target="_blank" rel="noopener noreferrer">
            <ViewerIcon />
          </a>
        </div>
      </li>
    );
  };

  renderUsages() {
    return (
      <ul className="detail__list">
        {this.props.usages.map(this.renderUsage)}
      </ul>
    );
  }

  render() {
    if (!this.props.usages) {
      return (<div className="baseline-margin">Fetching Usages...</div>);
    }

    if(this.props.usages.length === 0){
      return (
        <div>
          <div className="baseline-margin">No usages found</div>
        </div>
      );
    } else {
      return (
        <div>
          {this.renderUsages()}
        </div>
      );
    }
  }
}

VideoUsages.propTypes = {
  usages: React.PropTypes.array.isRequired,
  video: React.PropTypes.object.isRequired,
  publishedVideo: React.PropTypes.object.isRequired
};
