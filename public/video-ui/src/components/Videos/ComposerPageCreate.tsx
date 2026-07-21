import React from 'react';
import { getVideoBlock } from '../../util/getVideoBlock';
import Icon from '../Icon';
import { getStore } from '../../util/storeAccessor';
import { canonicalVideoPageExists } from '../../util/canonicalVideoPageExists';
import ErrorMessages from '../../constants/ErrorMessages';
import { createVideoPage } from '../../actions/VideoActions/videoPageCreate';
import { UsageState } from '../../slices/usage';

type Props = {
  video: any;
  error: Error;
  createVideoPage: typeof createVideoPage;
  usages: UsageState;
  videoEditOpen: boolean;
  requiredComposerFieldsMissing: any;
};

type State = {
  composerUpdateInProgress: boolean;
};

export default class ComposerPageCreate extends React.Component<Props, State> {
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
    } else {
      console.log('Could not find composer id');
    }
  };

  isHosted = () => {
    return this.props.video.category === 'Hosted';
  };

  usageErrorsExist = () => {
    // @ts-expect-error TS(2367): This comparison appears to be unintentional becaus... Remove this comment to see the full error message
    return this.props.error === ErrorMessages.usages;
  };

  getComposerLink = () =>
    `${this.getComposerUrl()}/content/${this.getComposerId()}`;

  pageCreate = () => {
    this.setState({
      composerUpdateInProgress: true
    });

    return (
      this.props.createVideoPage(
        this.props.video.id,
        this.props.video,
        getStore().getState().config.isTrainingMode
      ) as any
    ).then(() => {
      this.setState({
        composerUpdateInProgress: false
      });
    });
  };

  render() {
    const { usages, videoEditOpen, requiredComposerFieldsMissing } = this.props;

    if (canonicalVideoPageExists(usages)) {
      return (
        <a
          className="button__secondary"
          href={this.getComposerLink()}
          target="_blank"
          rel="noreferrer"
        >
          <Icon icon="pageview">Open in Composer</Icon>
        </a>
      );
    } else if (this.isHosted()) {
      return null;
    } else {
      const helpMsg = this.usageErrorsExist()
        ? 'Cannot create a video page because of errors in fetching video usages'
        : '';

      return (
        <span data-tip={helpMsg}>
          <button
            className="button__secondary"
            onClick={this.pageCreate}
            disabled={
              videoEditOpen ||
              this.state.composerUpdateInProgress ||
              requiredComposerFieldsMissing() ||
              this.usageErrorsExist()
            }
          >
            <Icon icon="add_to_queue">Create Video Page</Icon>
          </button>
        </span>
      );
    }
  }
}
