import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';
import {isVideoPublished} from '../../util/isVideoPublished';
import {hasUnpublishedChanges} from '../../util/hasUnpublishedChanges';
import Icon from '../../components/Icon';
import {getPublishErrors} from '../../util/getPublishErrors';

export default class VideoPublishBar extends React.Component {

  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing === saveStateVals.inprogress;
  }

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(this.props.video, this.props.publishedVideo);
  }

  videoIsPublishable() {
    return getPublishErrors(this.props.video).errors.length === 0;
  }

  isPublishingDisabled() {
    return this.videoIsCurrentlyPublishing() ||
      !this.videoHasUnpublishedChanges() ||
      !this.videoIsPublishable();
  }

  renderPublishButtonText() {
    if (this.videoIsCurrentlyPublishing()) {
      return (<span>Publishing</span>);
    }

    if (isVideoPublished(this.props.publishedVideo) && !this.videoHasUnpublishedChanges()){
      return (<span>Published</span>);

    }

    if (!this.videoIsPublishable()) {
      return (<span>
        <i className="icon icon__warning">warning</i>
        <span>Publish</span>
      </span>);
    } else {
      return (<span>Publish</span>);
    }

  }

  renderPublishButton() {
    return (<button
        type="button"
        className="btn"
        disabled={this.isPublishingDisabled()}
        onClick={this.props.publishVideo}
      >
        {this.renderPublishButtonText()}
      </button>
    );
  }

  renderVideoPublishedInfo() {
    if (isVideoPublished(this.props.publishedVideo)) {
      return <div className="publish__label label__live">Live</div>;
    }
    return <div className="publish__label label__draft">Draft</div>;
  }


  render() {

    if (!this.props.video) {
        return false;
    }

    return (
      <div className="flex-container flex-grow publish-bar">
        {this.renderVideoPublishedInfo()}
        <div className="flex-spacer"></div>
        {this.renderPublishButton()}
      </div>
    );
  }
}
