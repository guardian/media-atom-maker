import React from 'react';
import { isVideoPublished } from '../../util/isVideoPublished';
import { hasUnpublishedChanges } from '../../util/hasUnpublishedChanges';
import ScheduledLaunch from '../ScheduledLaunch/ScheduledLaunch';
import { canonicalVideoPageExists } from '../../util/canonicalVideoPageExists';
import VideoUtils from '../../util/video';

export default class VideoPublishBar extends React.Component {
  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing;
  }

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(
      this.props.video,
      this.props.publishedVideo
    );
  }

  isPublishingDisabled() {
    return (
      VideoUtils.getScheduledLaunch(this.props.video) ||
      VideoUtils.getEmbargo(this.props.video) ||
      this.videoIsCurrentlyPublishing() ||
      this.props.videoEditOpen ||
      !this.videoHasUnpublishedChanges() ||
      (canonicalVideoPageExists(this.props.usages) && this.props.requiredComposerFieldsMissing())
    );
  }

  publishedCanonicalVideoPageExists() {
    return this.props.usages &&
      this.props.usages.data &&
      this.props.usages.data.published &&
      this.props.usages.data.published.video &&
      this.props.usages.data.published.video.length > 0;

  }

  publishVideo = () => {

    if (this.publishedCanonicalVideoPageExists()) {

      this.props.updateVideoPage(
        this.props.video,
        this.props.usages.data,
        'published'
      );
    }

    this.props.publishVideo();
  };

  hasPublishedVideoPageUsages = () =>
    this.props.usages.data.published.video.length > 0;

  renderPublishButtonText() {
    if (this.videoIsCurrentlyPublishing()) {
      return <span>Publishing</span>;
    }

    if (isVideoPublished(this.props.publishedVideo)){
      if (this.videoHasUnpublishedChanges()) {
        return <span>Save and launch</span>;
      }

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

  renderScheduler() {
    if (isVideoPublished(this.props.publishedVideo)) {
      return;
    }

    return (
      <ScheduledLaunch
        video={this.props.video}
        videoEditOpen={this.props.videoEditOpen}
        saveVideo={this.props.saveVideo}
        hasPublishedVideoPageUsages={this.hasPublishedVideoPageUsages}
      />
    );
  }

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
