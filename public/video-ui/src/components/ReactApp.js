import React from 'react';

import Header from './Header';

class ReactApp extends React.Component {
  constructor(props) {
    super(props);
  }

  state = {
    fetchedVideoFor: ''
  };

  componentWillMount() {
    if (this.props.params.id) {
      this.props.appActions.getVideo(this.props.params.id);
      this.props.appActions.getPublishedVideo(this.props.params.id);
      this.props.appActions.getUploads(this.props.params.id);
      this.props.appActions.getUsages(this.props.params.id);
    }
  }

  componentWillReceiveProps() {
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
  }

  updateSearchTerm = searchTerm => {
    this.props.appActions.updateSearchTerm(searchTerm);
  };

  getEditableFields = () => {
    const allFields = this.props.checkedFormFields;

    const editableFormFields = Object.keys(
      allFields
    ).reduce((fields, formName) => {
      return fields.concat(Object.keys(this.props.checkedFormFields[formName]));
    }, []);
    return editableFormFields;
  };

  render() {
    return (
      <div className="wrap">
        <Header
          updateSearchTerm={this.updateSearchTerm}
          searchTerm={this.props.searchTerm}
          currentPath={this.props.location.pathname}
          video={this.props.video || {}}
          publishedVideo={this.props.publishedVideo || {}}
          showPublishedState={
            this.props.params.id ||
              this.props.location.pathname === '/videos/create'
              ? true
              : false
          }
          s3Upload={this.props.s3Upload}
          publishVideo={this.props.appActions.publishVideo}
          saveState={this.props.saveState}
          editableFields={this.getEditableFields()}
          updateVideoPage={this.props.appActions.updateVideoPage}
          createVideoPage={this.props.appActions.createVideoPage}
          videoEditOpen={this.props.videoEditOpen}
          usages={this.props.usages}
          presenceConfig={this.props.config.presence}
        />
        {this.props.error
          ? <div className="error-bar">{this.props.error}</div>
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
import * as updateSearchTerm from '../actions/SearchActions/updateSearchTerm';
import * as getVideo from '../actions/VideoActions/getVideo';
import * as getPublishedVideo from '../actions/VideoActions/getPublishedVideo';
import * as publishVideo from '../actions/VideoActions/publishVideo';
import * as saveVideo from '../actions/VideoActions/saveVideo';
import * as getUploads from '../actions/UploadActions/getUploads';
import * as videoPageUpdate from '../actions/VideoActions/videoPageUpdate';
import * as videoPageCreate from '../actions/VideoActions/videoPageCreate';
import * as videoUsages from '../actions/VideoActions/videoUsages';

function mapStateToProps(state) {
  return {
    searchTerm: state.searchTerm,
    saveState: state.saveState,
    video: state.video,
    publishedVideo: state.publishedVideo,
    error: state.error,
    uploads: state.uploads,
    s3Upload: state.s3Upload,
    checkedFormFields: state.checkedFormFields,
    videoEditOpen: state.videoEditOpen,
    usages: state.usage,
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    appActions: bindActionCreators(
      Object.assign(
        {},
        updateSearchTerm,
        getVideo,
        getPublishedVideo,
        publishVideo,
        saveVideo,
        getUploads,
        videoPageUpdate,
        videoPageCreate,
        videoUsages
      ),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(ReactApp);
