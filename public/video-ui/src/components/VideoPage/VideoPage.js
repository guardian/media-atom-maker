import React from 'react';
import Q from 'q';
import {fetchComposerId} from '../../services/capi.js';
import {getVideoBlock} from '../../util/getVideoBlock';
import {getStore} from '../../util/storeAccessor';

export default class VideoPage extends React.Component {

  state = {
    usages: undefined,
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

  getComposerIds = (usages) => {

    return Q.all(usages.map(fetchComposerId))
    .then(ids => {
      const composerIdsWithUsage = ids.reduce((idsWithUsage, composerId, index) => {
        if (composerId !== '') {
          idsWithUsage.push({usage: usages[index], composerId: composerId});
        }
        return idsWithUsage;
      }, []);
      return this.setState({
        composerIds: composerIdsWithUsage
      });
    })
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
    const noComposer = !((this.props.composerPageWithUsage && this.props.composerPageWithUsage.composerId) || (composerUsages && composerUsages.length > 0));

    return !((this.props.composerPageWithUsage && this.props.composerPageWithUsage.composerId) || (composerUsages && composerUsages.length > 0));
  }

  renderCreateButton = () => {
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

      return (
        <div>Publish this atom to enable the creation of composer pages</div>
      );
    {this.renderCreateButton()}
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

  arraysFetched() {
    const fetched = this.props.usages && this.props.usages.usagesWithoutComposer !== undefined && this.props.usages.composerIdsWithUsage !== undefined;

    return this.props.usages && this.props.usages.usagesWithoutComposer !== undefined && this.props.usages.composerIdsWithUsage !== undefined;
  }

  render() {

    if (!this.arraysFetched()) {
      return (<div className="baseline-margin">Fetching Usages...</div>)
>>>>>>> composer usages and usages appear on the same page
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

