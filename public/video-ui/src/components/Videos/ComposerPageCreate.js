import React from 'react';
import { getVideoBlock } from '../../util/getVideoBlock';
import Icon from '../Icon';
import { getStore } from '../../util/storeAccessor';
import { canonicalVideoPageExists } from '../../util/canonicalVideoPageExists';

export default class ComposerPageCreate extends React.Component {
  state = {
    composerUpdateInProgress: false
  };

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  getComposerId = () => {
    const usages = getStore().getState().usage.data;
    const videoPages = [...usages.preview.video, ...usages.published.video];
    if (videoPages.length !== 0) {
      return videoPages[0].fields.internalComposerCode;
    }
    else {
      console.log("Could not find composer id");
    }
  }

  isHosted = () => {
    return this.props.video.category === 'Hosted';
  };

  getComposerLink = () => `${this.getComposerUrl()}/content/${this.getComposerId()}`;

  pageCreate = () => {
    this.setState({
      composerUpdateInProgress: true
    });

    const videoBlock = getVideoBlock(
      this.props.video.id,
      this.props.video.title,
      this.props.video.source
    );

    return this.props
      .createVideoPage(
        this.props.video.id,
        this.props.video,
        this.getComposerUrl(),
        videoBlock,
        getStore().getState().config.isTrainingMode
      )
      .then(() => {
        this.setState({
          composerUpdateInProgress: false
        });
      });
  };

  render() {

    const { usages, videoEditOpen, requiredComposerFieldsMissing } = this.props;
    const showOpenPage = canonicalVideoPageExists(usages) || this.isHosted();

    if (showOpenPage) {
      return (
        <a className="button__secondary" href={this.getComposerLink()} target="_blank">
          <Icon icon="pageview">Open in Composer</Icon>
        </a>
      );
    }

    else {
      return (
        <button
          className="button__secondary"
          onClick={this.pageCreate}
          disabled={videoEditOpen || this.state.composerUpdateInProgress || requiredComposerFieldsMissing()}
        >
          <Icon icon="add_to_queue">Create Video Page</Icon>
        </button>
      );
    }
  }
}
