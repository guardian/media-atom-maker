import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';
import {isVideoPublished} from '../../util/isVideoPublished';
import {hasUnpublishedChanges} from '../../util/hasUnpublishedChanges';

export default class VideoPublishBar extends React.Component {

  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing === saveStateVals.inprogress;
  }

  renderUnpublishedNote() {
    return (
      <span className="publish-bar__message__block">
        <i className="icon">repeat</i>
        <span className="bar__message">This video atom has unpublished changes</span>
      </span>
    );
  }

  renderPublishButton() {
    return (<button
        type="button"
        className="btn"
        disabled={!hasUnpublishedChanges(this.props.video, this.props.publishedVideo) || this.videoIsCurrentlyPublishing()}
        className="bar__button publish-bar__button"
        onClick={this.props.publishVideo}
      >
        Publish
      </button>
    );
  }

  renderPublishMessage() {
    return (
      <span className="bar__message publish-bar__message">Publishing...</span>
    );
  }

  renderVideoPublishedInfo() {
    if (isVideoPublished(this.props.publishedVideo)) {
      return <div className="publish__label label__live">Live</div>
    }
    return <div className="publish__label label__draft">Draft</div>;
  }


  render() {

    if (!this.props.video || !this.props.video.contentChangeDetails) {
        return false;
    }

    if (this.videoIsCurrentlyPublishing()) {
      return (
        <div className="flex-container flex-grow publish-bar">
          {this.renderVideoPublishedInfo()}
          <div className="flex-spacer"></div>
          {this.renderPublishButton()}
          {this.renderPublishMessage()}
        </div>
      );
    }

    if (!hasUnpublishedChanges(this.props.video, this.props.publishedVideo)) {
      return (
        <div className="flex-container flex-grow publish-bar">
          {this.renderVideoPublishedInfo()}
          <div className="flex-spacer"></div>
          {this.renderPublishButton()}
        </div>
      );
    }

    return (
      <div className="flex-container flex-grow publish-bar">
        {this.renderVideoPublishedInfo()}
        <div className="flex-spacer"></div>
        {this.renderUnpublishedNote()}
        {this.renderPublishButton()}
      </div>
    );
  }
}
