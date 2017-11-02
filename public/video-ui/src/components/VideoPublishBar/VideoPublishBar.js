import React from 'react';
import { saveStateVals } from '../../constants/saveStateVals';
import { isVideoPublished } from '../../util/isVideoPublished';
import { hasUnpublishedChanges } from '../../util/hasUnpublishedChanges';
import { getStore } from '../../util/storeAccessor';
import ScheduledLaunch from '../../components/ScheduledLaunch/ScheduledLaunch';
import { getVideoBlock } from '../../util/getVideoBlock';
import { publishedCanonicalVideoPageExists } from '../../util/publishedCanonicalVideoPageExists';
import { canonicalVideoPageExists } from '../../util/canonicalVideoPageExists';

export default class VideoPublishBar extends React.Component {
  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing === saveStateVals.inprogress;
  }

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(
      this.props.video,
      this.props.publishedVideo,
      this.props.editableFields
    );
  }

  isPublishingDisabled() {
    return (
      this.props.video.contentChangeDetails && this.props.video.contentChangeDetails.scheduledLaunch ||
      this.videoIsCurrentlyPublishing() ||
      this.props.videoEditOpen ||
      !this.videoHasUnpublishedChanges() ||
      (canonicalVideoPageExists(this.props.usages) && this.props.requiredComposerFieldsMissing())
    );
  }

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  };

  publishVideo = () => {

    if (publishedCanonicalVideoPageExists(this.props.usages)) {

      const videoBlock = getVideoBlock(
        this.props.video.id,
        this.props.video.title,
        this.props.video.source
      );

      this.props.updateVideoPage(
        this.props.video,
        this.getComposerUrl(),
        videoBlock,
        this.props.usages,
        'published'
      );
    }

    this.props.publishVideo();
  };

  renderPublishButtonText() {
    if (this.videoIsCurrentlyPublishing()) {
      return <span>Publishing</span>;
    }

    if (
      isVideoPublished(this.props.publishedVideo) &&
      !this.videoHasUnpublishedChanges()
    ) {
      return <span>Published</span>;
    }

    return <span>Publish</span>;
  }

  renderPublishButton() {
    return (
      <button
        type="button"
        className="btn"
        disabled={this.isPublishingDisabled()}
        onClick={this.publishVideo}
      >
        {this.renderPublishButtonText()}
      </button>
    );
  }

  renderScheduler = () =>
    this.props.query.showScheduler === "true" &&
    <ScheduledLaunch
      video={this.props.video}
      videoEditOpen={this.props.videoEditOpen}
      saveVideo={this.props.saveVideo}
    />;

  render() {
    if (!this.props.video) {
      return false;
    }
    return (
      <div className="flex-container publish-bar">
        {this.renderScheduler()}
        {this.renderPublishButton()}
      </div>
    );
  }
}
