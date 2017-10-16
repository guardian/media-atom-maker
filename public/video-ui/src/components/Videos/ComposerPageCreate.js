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

  getComposerId = () => getStore().getState().usage.data.preview.video[0].fields.internalComposerCode;

  isHosted = () => {
    return this.props.video.category === 'Hosted';
  };

  openComposerPage = () => window.open(`${this.getComposerUrl()}/content/${this.getComposerId()}`, '_blank');

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

    return (
      <button
        className="button__secondary"
        onClick={showOpenPage ? this.openComposerPage : this.pageCreate}
        disabled={
          videoEditOpen || this.state.composerUpdateInProgress || requiredComposerFieldsMissing()
        }
      >
        { showOpenPage ? <span>Open Composer Page</span> : <span><Icon icon="add_to_queue" /> Create Video Page</span> }
      </button>
    );
  }
}