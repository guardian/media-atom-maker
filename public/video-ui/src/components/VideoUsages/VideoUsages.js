import React from 'react';
import {getVideoBlock} from '../../util/getVideoBlock';
import {getStore} from '../../util/storeAccessor';
import {isVideoPublished} from '../../util/isVideoPublished';
import {hasUnpublishedChanges} from '../../util/hasUnpublishedChanges';
import {FrontendIcon, ComposerIcon} from '../Icon';

export default class VideoUsages extends React.Component {

  state = {
    pageCreated: false
  };

  componentDidMount() {
    if (this.props.video) {
      this.props.fetchUsages(this.props.video.id);
    }
  }

  componentWillReceiveProps(newProps) {
    const oldVideoId = this.props.video && this.props.video.id;
    const newVideoId = newProps.video && newProps.video.id;

    if (oldVideoId !== newVideoId) {

      this.props.fetchUsages(newVideoId);
    }
  }

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
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

  noExistingComposerPages = (composerUsages) => {
    return !((this.props.composerPageWithUsage && this.props.composerPageWithUsage.composerId) || (composerUsages && composerUsages.length > 0));
  };

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(this.props.video, this.props.publishedVideo);
  }

  renderCreateButton = () => {
    if (this.props.video && isVideoPublished(this.props.publishedVideo) && !this.videoHasUnpublishedChanges()) {
      return (
        <button
          type="button"
          className="btn page__add__button"
          disabled={this.state.pageCreated}
          onClick={this.pageCreate}>
          Create video page
        </button>
      );
    }

    return (<div>Publish this atom to enable the creation of composer pages</div>);
  };

  renderUsage = (usage) => {
    const composerLink = `${this.getComposerUrl()}/find-by-path/${usage}`;
    const websiteLink = `https://gu.com/${usage}`;

    return (
      <li key={usage} className="detail__list__item">
        <a className="usage--platform-link" href={websiteLink} title="Open on theguardian.com" target="_blank">
          <FrontendIcon />
        </a>
        <a className="usage--platform-link" href={composerLink} title="Open in Composer" target="_blank">
          <ComposerIcon />
        </a>
        {usage}
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
    if (! this.props.usages) {
      return (<div className="baseline-margin">Fetching Usages...</div>);
    }

    return (
      <div>
        {this.renderCreateButton()}
        {this.renderUsages()}
      </div>
    );
  }
}

