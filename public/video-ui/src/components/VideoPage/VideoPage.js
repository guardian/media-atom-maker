import React from 'react';
import Q from 'q';
import {createComposerPage} from '../../services/flexible.js';
import {fetchComposerId} from '../../services/capi.js';

import {getStore} from '../../util/storeAccessor';

export default class VideoPage extends React.Component {

  state = {
    composerIds: undefined
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

    const videoPage = {
      elements: [
        {
          elementType: 'content-atom',
          fields: {
            id: this.props.video.id,
            atomType: 'media',
            required: 'true',
            title: this.props.video.title,
            published: 'Unable to get published state from atom',
            isMandatory: 'true',
            editorialLink: ''

          },
          assets: []
        }
      ]
    };
    return createComposerPage(this.props.video.id, this.props.video.title, videoPage)
    .then(() => {
      this.props.fetchUsages(this.props.video.id);
    });
  }

  renderComposerLink(composerIdWithUsage) {

    const composerUrl = getStore().getState().config.composerUrl;

    return (
      <li key={composerIdWithUsage.composerId} className="detail__list__item">
        <a href= {composerUrl + '/content/' + composerIdWithUsage.composerId}>{composerIdWithUsage.usage}</a>
      </li>
      );
  }


  render() {

    if (this.state.composerIds && this.state.composerIds.length !== 0) {
      return (
        <ul className="detail__list">
          {this.state.composerIds.map(this.renderComposerLink)}
        </ul>
      );
    }

    return (<button type="button" className="btn page__add__button" onClick={this.pageCreate}>Create video page</button>);
  }
}

