import React from 'react';
import {browserHistory} from 'react-router';
import Icon from '../Icon';

class VideoUpload extends React.Component {
  getUploadPolicy = (event) => {
    if(!this.props.video)
      return;

    if(event.target.files.length == 0) {
      this.props.uploadActions.clearUploadPolicy();
    } else {
      this.props.uploadActions.getUploadPolicy(this.props.video.id, event.target.files[0]);
    }
  };

  startUpload = () => {
    if(!this.props.upload)
      return;

    this.props.uploadActions.startUpload(this.props.upload.policy, this.props.upload.file);
  };

  goToAtomPage = () => {
    const atomId = this.props.video ? this.props.video.id : null;

    if(atomId) {
      this.props.uploadActions.clearUploadPolicy();
      browserHistory.push(`/videos/${atomId}`);
    }
  };

  header() {
    return <div className="upload__header__container">
      <header className="upload__header">Upload Video</header>
      <button type="button" onClick={this.goToAtomPage}>
        <Icon className="icon__edit" icon="clear" />
      </button>
    </div>;
  }

  uploadButton() {
    return <button type="button" className="button__secondary" disabled={!this.props.upload} onClick={this.startUpload}>
      <Icon icon="backup">Upload</Icon>
    </button>;
  }

  uploadedNotification() {
    return <button type="button" className="button__secondary" disabled={true}>
      <Icon icon="done">Uploaded</Icon>
    </button>;
  }

  progress() {
    return <div className="upload__progress">
      <progress value={this.props.upload.progress} max={1.0} />
    </div>;
  }

  leftHandSide(uploading, complete) {
    if(complete) {
      return this.uploadedNotification();
    } else if(uploading) {
      return this.progress();
    } else {
      return this.uploadButton();
    }
  }

  picker() {
    const uploading = this.props.upload && this.props.upload.progress;
    const complete = this.props.upload && this.props.upload.complete;

    return <div className="upload__picker">
      <p>File</p>
      <div className="flex-container">
        <input type="file" onChange={this.getUploadPolicy} disabled={uploading} />
        {this.leftHandSide(uploading, complete)}
      </div>
    </div>;
  }

  render() {
    return <div className="upload">
      <div className="upload__main">
        {this.header()}

        <div className="form__group">
          {this.picker()}
        </div>
      </div>
    </div>;
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as policyActions from '../../actions/UploadActions/uploadPolicy';
import * as startUpload from '../../actions/UploadActions/upload';

function mapStateToProps(state) {
  return {
    video: state.video,
    upload: state.upload
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo), dispatch),
    uploadActions: bindActionCreators(Object.assign({}, policyActions, startUpload), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);
