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

  isHosted = () => {
    return this.props.video.category === 'Hosted';
  };

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
    if (this.props.canonicalVideoPageExists() || this.isHosted()) {
      return null;
    }

    return (
      <button
        className="button__secondary"
        onClick={this.pageCreate}
        disabled={
          this.props.videoEditOpen || this.state.composerUpdateInProgress || this.props.requiredComposerFieldsMissing()
        }
      >
        <Icon icon="add_to_queue" /> Create Video Page
      </button>
    );
  }
}
