import React from 'react';
import { getVideoBlock } from '../../util/getVideoBlock';
import Icon from '../Icon';
import { getStore } from '../../util/storeAccessor';

export default class ComposerPageCreate extends React.Component {
  state = {
    composerUpdateInProgress: false
  };

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  getComposerId = () => {
    const usages = getStore().getState().usage.data;
    if (usages.preview.video.length && !usages.published.video.length) {
      return usages.preview.video[0].fields.internalComposerCode;
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
    const { canonicalVideoPageExists, videoEditOpen, requiredComposerFieldsMissing } = this.props;
    const showOpenPage = canonicalVideoPageExists() || this.isHosted();
    
    if (showOpenPage) {
      return <a className="button__secondary" href={this.getComposerLink()} target="_blank">Open in Composer</a>;
    } 
    else {
      return (
        <button
          className="button__secondary"
          onClick={this.pageCreate}
          disabled={videoEditOpen || this.state.composerUpdateInProgress || requiredComposerFieldsMissing()}
        >
          <span><Icon icon="add_to_queue" /> Create Video Page</span>
        </button>
      );
    }
  }
}