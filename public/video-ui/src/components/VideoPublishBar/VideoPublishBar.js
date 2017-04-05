import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';
import {isVideoPublished, hasVideoExpired} from '../../util/isVideoPublished';
import {hasUnpublishedChanges} from '../../util/hasUnpublishedChanges';

class VideoPublishBar extends React.Component {

  videoIsCurrentlyPublishing() {
    return this.props.saveState.publishing === saveStateVals.inprogress;
  }

  videoHasUnpublishedChanges() {
    return hasUnpublishedChanges(this.props.video, this.props.publishedVideo);
  }

  isPublishingDisabled() {
    return this.videoIsCurrentlyPublishing() ||
      this.props.editState.metadataEditable ||
      this.props.editState.youtubeEditable ||
      !this.videoHasUnpublishedChanges();
  }

  renderPublishButtonText() {
    if (this.videoIsCurrentlyPublishing()) {
      return (<span>Publishing</span>);
    }

    if (isVideoPublished(this.props.publishedVideo) && !this.videoHasUnpublishedChanges()){
      return (<span>Published</span>);

    }

    return (<span>Publish</span>);

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
    if (hasVideoExpired(this.props.publishedVideo)) {
      return <div className="publish__label label__expired">Expired</div>;
    } else if (isVideoPublished(this.props.publishedVideo)) {
      return <div className="publish__label label__live">Live</div>;
    }
    return <div className="publish__label label__draft">Draft</div>;
  }

  renderEmbedButton() {
    // you can always select a video in 'preview' mode (this is used by the Pluto embed)
    // otherwise you should only be able to select published videos (when embedded inside Composer for example)

    const embedButton = <button type="button" className="btn" onSelectVideo={this.selectVideo} publishedVideo={this.props.publishedVideo} onClick={this.props.onSelectVideo}>Select this Video</button>;

    switch(this.props.embeddedMode) {
      case "preview":
        return embedButton;

      case "live":
        if(isVideoPublished(this.props.publishedVideo) && !this.videoHasUnpublishedChanges()) {
          return embedButton;
        } else {
          return <div>This atom cannot be embedded because it has not been published</div>;
        }

      default:
        return false;
    }
  }


  render() {

    if (!this.props.video) {
        return false;
    }

    return (
      <div className="flex-container flex-grow publish-bar">
        {this.renderVideoPublishedInfo()}
        <div className="flex-spacer"></div>
        <div className="icon__spacing">
          {this.renderEmbedButton()}
        </div>
        {this.renderPublishButton()}
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';

function mapStateToProps(state) {
  return {
    editState: state.editState
  };
}
export default connect(mapStateToProps)(VideoPublishBar);
