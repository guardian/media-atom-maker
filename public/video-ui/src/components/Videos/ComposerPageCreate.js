import React from 'react';
import { getVideoBlock } from '../../util/getVideoBlock';
import Icon from '../Icon';
import { getStore } from '../../util/storeAccessor';
import { getComposerPages } from '../../util/getComposerPages';

export default class ComposerPageCreate extends React.Component {
  state = {
    composerUpdateInProgress: false
  };

  getVideoMetadata = () => {
    return {
      headline: this.props.video.title,
      standfirst: this.props.video.description
        ? '<p>' + this.props.video.description + '</p>'
        : null
    };
  };

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  composerPageExists = () => {
    return getComposerPages(this.props.usages).length > 0;
  };

  pageCreate = () => {
    this.setState({
      composerUpdateInProgress: true
    });

    const metadata = this.getVideoMetadata();

    const videoBlock = getVideoBlock(this.props.video.id, metadata);

    return this.props
      .createVideoPage(
        this.props.video.id,
        metadata,
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
    if (this.composerPageExists()) {
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
