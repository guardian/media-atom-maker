import React from 'react';
import Q from 'q';
import {getVideoBlock} from '../../util/getVideoBlock';
import {getStore} from '../../util/storeAccessor';

export default class VideoPage extends React.Component {

  state = {
    pageCreated: false
  }

  componentDidMount() {
    if (this.props.video) {
      this.props.fetchUsages(this.props.video.id)
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
  }

  pageCreate = () => {

    this.setState({
      pageCreated: true
    });

    const videoPage = getVideoBlock(this.props.video.id, this.props.video.title);

    return this.props.createComposerPage(this.props.video.id, this.props.video.title, this.getComposerUrl(), videoPage)
  }

  renderComposerLink = (composerIdWithUsage) => {
    const composerUrl = this.getComposerUrl();

    return (
      <li key={composerIdWithUsage.composerId} className="detail__list__item">
        <a href= {composerUrl + '/content/' + composerIdWithUsage.composerId}>{composerIdWithUsage.usage}</a>
      </li>
      );
  }

  noExistingComposerPages = (composerUsages) => {
    return !((this.props.composerPageWithUsage && this.props.composerPageWithUsage.composerId) || (composerUsages && composerUsages.length > 0));
  }

  usagesFetched() {
    return this.props.usages && this.props.usages.usagesWithoutComposer !== undefined && this.props.usages.composerIdsWithUsage !== undefined;
  }


  renderCreateButton = () => {
    if (this.props.video && this.props.video.contentChangeDetails && this.props.video.contentChangeDetails.published) {
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
  }

  renderComposerPages = () => {

    //If composerId exists, this means that this is the only composer page
    //that exists, it has just been created via media-atom-maker. It may not
    //yet be in capi, so we use this id to render it
    if (this.props.composerPageWithUsage && this.props.composerPageWithUsage.composerId) {
      return (
        <ul className="detail__list">
          {this.renderComposerLink(this.props.composerPageWithUsage)}
        </ul>
      );
    }

    //Else we are safe to get composer pages from composerIds list derived from usages
    const composerUsages = this.props.usages.composerIdsWithUsage;

    if (composerUsages && composerUsages.length > 0) {

      return (
        <ul className="detail__list">
          {composerUsages.map(this.renderComposerLink)}
        </ul>
      );
    }

    //If there are no composer pages, display a button that allows for creating one
    else {
      return (
        <div>
          {this.renderCreateButton()}
        </div>
      );
    }
  }

  renderUsage = (usage) => {
    return (
      <li key={usage} className="detail__list__item">
        {usage}
      </li>
    )
  }

  renderUsages() {
    return (
      <ul className="detail__list">
        {this.props.usages.usagesWithoutComposer.map(this.renderUsage)}
      </ul>
    )
  }

  render() {

    if (!this.usagesFetched()) {
      return (<div className="baseline-margin">Fetching Usages...</div>)
    }

    if (this.props.usages.usagesWithoutComposer.length === 0) {
      if (this.noExistingComposerPages(this.props.usages.composerIdsWithUsage)) {
        return (
          <div>
            {this.renderCreateButton()}
          <div className="baseline-margin">No usages found</div>
          </div>
        );
      }
      return (<div>{this.renderComposerPages()}</div>)
    }

    return (<div>{this.renderUsages()} {this.renderComposerPages()}</div>)
  }
}

