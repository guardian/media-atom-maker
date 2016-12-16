import React from 'react';
import Q from 'q';
import {fetchComposerId} from '../../services/capi.js';
import {getVideoBlock} from '../../util/getVideoBlock';
import {getStore} from '../../util/storeAccessor';

export default class VideoPage extends React.Component {

  state = {
    composerIds: undefined,
    pageCreated: false
  }

  componentDidMount() {
    if (this.props.usages) {
      this.getComposerIds(this.props.usages);
    }
  }

  componentWillReceiveProps(newProps) {
    const oldVideoId = this.props.video && this.props.video.id;
    const newVideoId = newProps.video && newProps.video.id;
    const oldUsages = this.props.usages;
    const newUsages = newProps.usages;

    if (oldVideoId !== newVideoId || oldUsages !== newUsages) {
      this.getComposerIds(newProps.usages);
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

    return this.props.createComposerPage(this.props.video.id, this.props.video.title, this.getComposerUrl(), videoPage);
  }

  renderComposerLink = (composerIdWithUsage) => {
    const composerUrl = this.getComposerUrl();

    return (
      <li key={composerIdWithUsage.composerId} className="detail__list__item">
        <a href= {composerUrl + '/content/' + composerIdWithUsage.composerId}>{composerIdWithUsage.usage}</a>
      </li>
      );
  }

  render() {
    //If composerId exists, this means that this is the only composer page
    //that exists, it has just been created via media-atom-maker. It may not
    //yet be in capi, so we use this id to render it
    if (this.props.composerPageWithUsage.composerId) {
      return (
        <ul className="detail__list">
          {this.renderComposerLink(this.props.composerPageWithUsage)}
        </ul>
      );
    }

    //Else we are safe to get composer pages from composerIds list derived from usages
    if (this.state.composerIds && this.state.composerIds.length > 0) {

      return (
        <ul className="detail__list">
          {this.state.composerIds.map(id => {
            return this.renderComposerLink(id);
           })}
        </ul>
      );
    }

    //If there are no composer pages, display a button that allows for creating one
    else {
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
  }
}

