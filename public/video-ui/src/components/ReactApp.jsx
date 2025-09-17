import React from 'react';

import Header from './Header';

class ReactApp extends React.Component {
  constructor(props) {
    super(props);
    if (this.props.params.id) {
      this.props.appActions.getVideo(this.props.params.id);
      this.props.appActions.getPublishedVideo(this.props.params.id);
      this.props.appActions.getUploads(this.props.params.id);
      this.props.appActions.getUsages(this.props.params.id);
    }

    this.state = {
      fetchedVideoFor: ''
    };
  }

  componentDidUpdate(prevProps) {
    if (
      this.props.params.id &&
      (!this.props.video || this.props.params.id !== this.props.video.id) &&
      this.state.fetchedVideoFor !== this.props.params.id
    ) {
      this.props.appActions.getVideo(this.props.params.id);
      this.props.appActions.getPublishedVideo(this.props.params.id);
      this.props.appActions.getUploads(this.props.params.id);
      this.setState({
        fetchedVideoFor: this.props.params.id
      });
    }

    if(this.props.error.key !== prevProps.error.key ) {
      document.body.scrollIntoView({block: 'start', behavior: 'smooth'});
    }
  }

  componentDidMount() {
    window.addEventListener('keyup', this.handleKeyUp);
  }

  componentWillUnmount() {
    window.removeEventListener('keyup', this.handleKeyUp);
  }

  handleKeyUp = (event) => {
    if (window.self !== window.top) {
      window.parent.postMessage({ eventKey: event.key }, '*');
    }
  };

  render() {
    const showPublishedState = this.props.params.id;

    return (
      <div className="wrap">
        <Header
          mediaPlatformFilter={this.props.mediaPlatformFilter}
          updateMediaPlatformFilter={this.props.appActions.updateMediaPlatformFilter}
          shouldUseCreatedDateForSort={this.props.shouldUseCreatedDateForSort}
          updateShouldUseCreatedDateForSort={this.props.appActions.updateShouldUseCreatedDateForSort}
          reportPresenceClientError={this.props.appActions.reportPresenceClientError}
          updateSearchTerm={this.props.appActions.updateSearchTerm}
          search={this.props.search}
          currentPath={this.props.location.pathname}
          video={this.props.video || {}}
          publishedVideo={this.props.publishedVideo || {}}
          showPublishedState={showPublishedState}
          s3Upload={this.props.s3Upload}
          publishVideo={this.props.appActions.publishVideo}
          saveState={this.props.saveState}
          updateVideoPage={this.props.appActions.updateVideoPage}
          createVideoPage={this.props.appActions.createVideoPage}
          videoEditOpen={this.props.videoEditOpen}
          usages={this.props.usages || {}}
          presenceConfig={this.props.config.presence}
          isTrainingMode={this.props.config.isTrainingMode}
          formFieldsWarning={this.props.formFieldsWarning}
          deleteVideo={this.props.appActions.deleteVideo}
          updateVideo={this.props.appActions.updateVideo}
          saveVideo={this.props.appActions.saveVideo}
          query={this.props.location.query}
          error={this.props.error}
        />
        {this.props.error.message
          ? <div
            key={this.props.error.key}
            className={`error-bar error-bar--animate`}
          >
            {this.props.error.message}
          </div>
          : false}
        <div>
          {this.props.children}
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as updateShouldUseCreatedDateForSort from '../actions/SearchActions/updateShouldUseCreatedDateForSort';
import * as updateMediaPlatformFilter from "../actions/SearchActions/updateMediaPlatformFilter";
import * as getVideo from '../actions/VideoActions/getVideo';
import * as getPublishedVideo from '../actions/VideoActions/getPublishedVideo';
import * as publishVideo from '../actions/VideoActions/publishVideo';
import * as saveVideo from '../actions/VideoActions/saveVideo';
import * as getUploads from '../actions/UploadActions/getUploads';
import * as videoPageUpdate from '../actions/VideoActions/videoPageUpdate';
import * as videoPageCreate from '../actions/VideoActions/videoPageCreate';
import * as videoUsages from '../actions/VideoActions/videoUsages';
import * as deleteVideo from '../actions/VideoActions/deleteVideo';
import * as updateVideo from '../actions/VideoActions/updateVideo';
import * as reportPresenceClientError from '../actions/PresenceActions/reportError';
import { updateSearchTerm } from "../slices/search";

function mapStateToProps(state) {
  return {
    search: state.search,
    shouldUseCreatedDateForSort: state.shouldUseCreatedDateForSort,
    mediaPlatformFilter: state.mediaPlatformFilter,
    saveState: state.saveState,
    video: state.video,
    publishedVideo: state.publishedVideo,
    error: state.error,
    uploads: state.uploads,
    s3Upload: state.s3Upload,
    videoEditOpen: state.videoEditOpen,
    usages: state.usage,
    config: state.config,
    formFieldsWarning: state.formFieldsWarning
  };
}

function mapDispatchToProps(dispatch) {
  return {
    appActions: bindActionCreators(
      Object.assign(
        { updateSearchTerm },
        updateShouldUseCreatedDateForSort,
        updateMediaPlatformFilter,
        getVideo,
        getPublishedVideo,
        publishVideo,
        saveVideo,
        getUploads,
        videoPageUpdate,
        videoPageCreate,
        videoUsages,
        deleteVideo,
        updateVideo,
        reportPresenceClientError
      ),
      dispatch
    )
  };
}

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(React.memo(ReactApp));
