import React from 'react';
import { getVideoBlock } from '../../util/getVideoBlock';
import Icon from '../Icon';
import { getStore } from '../../util/storeAccessor';
import { getComposerPages } from '../../util/getComposerPages';

export default class ComposerPageCreate extends React.Component {
  state = {
    composerUpdateInProgress: false
  };

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  composerPageExists = () => {
    return getComposerPages(this.props.usages).length > 0;
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
        videoBlock
      )
      .then(() => {
        this.setState({
          composerUpdateInProgress: false
        });
      });
  };

  render() {
    if (this.composerPageExists() || this.isHosted()) {
      return null;
    }

    return (
      <button
        className="button__secondary"
        onClick={this.pageCreate}
        disabled={
          this.props.videoEditOpen || this.state.composerUpdateInProgress
        }
      >
        <Icon icon="add_to_queue" /> Create Video Page
      </button>
    );
  }
}
