import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import SaveButton from '../utils/SaveButton';
import {blankVideoData} from '../../constants/blankVideoData';
import {formNames} from '../../constants/formNames';

class VideoCreate extends React.Component {

  componentDidMount() {
    if (!this.props.video) {
      this.props.videoActions.updateVideo(blankVideoData);
    }
  }

  createVideo = () => {
    this.props.videoActions.createVideo(this.props.video);
  };

  resetVideo = () => {
    this.props.videoActions.updateVideo(blankVideoData);
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  render () {
    return (
      <div className="container">
        <form className="form create-form">
          <h1>Create new video</h1>
          <VideoEdit
            video={this.props.video}
            updateVideo={this.updateVideo}
            editable={true}
            updateErrors={this.props.formErrorActions.updateFormErrors}
            formName={formNames.create}
          />
          <SaveButton
            video={this.props.video}
            formErrors={this.props.formErrors[formNames.create] ? this.props.formErrors[formNames.create] : {}}
            saveState={this.props.saveState}
            onSaveClick={this.createVideo}
            onResetClick={this.resetVideo} />
        </form>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createVideo from '../../actions/VideoActions/createVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';
import * as updateFormErrors from '../../actions/FormErrorActions/updateFormErrors';

function mapStateToProps(state) {
  return {
    video: state.video,
    saveState: state.saveState,
    formErrors: state.formErrors
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, updateVideo, createVideo), dispatch),
    formErrorActions: bindActionCreators(Object.assign({}, updateFormErrors), dispatch)

  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoCreate);
