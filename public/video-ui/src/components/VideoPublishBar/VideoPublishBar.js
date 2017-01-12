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

  render() {

    if (!this.props.video || !this.props.video.contentChangeDetails) {
        return false;
    }

    if (!this.videoHasUnpublishedChanges()) {
        return false;
    }

    if (this.videoIsCurrentlyPublishing()) {
      return (
        <div className="bar">
          <span className="bar__message">Publishing...</span>
        </div>
      );
    }

    return (
      <div className="bar">
        <i className="icon">repeat</i>
        <span className="bar__message">This video has unpublished meta data</span>
        <button type="button" className="bar__button" onClick={this.props.publishVideo}>Publish changes to Youtube</button>
      </div>
    )
  }
}
