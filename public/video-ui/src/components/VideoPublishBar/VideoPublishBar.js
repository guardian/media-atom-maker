import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';

export default class VideoPublishBar extends React.Component {

  videoHasUnpublishedChanges() {
    const changeDetails = this.props.video.contentChangeDetails
    const lastModified = changeDetails.lastModified && changeDetails.lastModified.date
    const published = changeDetails.published && changeDetails.published.date

    if (!published || lastModified > published) {
      return true
    }

    return false
  }

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
        disabled={!this.videoHasUnpublishedChanges() || this.videoIsCurrentlyPublishing()}
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

  render() {

    if (!this.props.video || !this.props.video.contentChangeDetails) {
        return false;
    }

    if (this.videoIsCurrentlyPublishing()) {
      return (
        <div className="bar publish-bar">
          {this.renderPublishButton()}
          {this.renderPublishMessage()}
        </div>
      );
    }

    if (!this.videoHasUnpublishedChanges()) {
      return (
        <div className="bar publish-bar">
          {this.renderPublishButton()}
        </div>
      );
    }

    return (
      <div className="bar publish-bar">
        {this.renderPublishButton()}
        {this.renderUnpublishedNote()}
      </div>
    )
  }
}
