import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';
import {isVideoPublished} from '../../util/isVideoPublished';
import {hasUnpublishedChanges} from '../../util/hasUnpublishedChanges';
import Icon from '../../components/Icon';
import {isPublishable} from '../../util/isPublishable';

export default class VideoPublishBar extends React.Component {

  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing === saveStateVals.inprogress;
  }

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(this.props.video, this.props.publishedVideo);
  }

  videoIsPublishable() {
    return isPublishable(this.props.video).errors.length === 0;
  }

  renderPublishButton() {
    if (this.videoIsCurrentlyPublishing()) {
      return (<button
          type="button"
          className="btn"
          disabled={true}
        >
          Publishing
        </button>
      );
    } else if (isVideoPublished(this.props.publishedVideo) && !this.videoHasUnpublishedChanges()) {
      return (<button
          type="button"
          className="btn"
          disabled={true}
        >
          Published
        </button>
      );
    } else {
      return (<button
          type="button"
          className="btn"
          disabled={!this.videoIsPublishable()}
          onClick={this.props.publishVideo}
        >
          Publish
        </button>
      );
    }
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
