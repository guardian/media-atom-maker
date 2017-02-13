import React from 'react';
import {Link} from 'react-router';
import Icon from '../Icon';

class VideoUpload extends React.Component {
  getUploadPolicy = (event) => {
    if (!this.props.video) {
      return;
    }

    if (event.target.files.length == 0) {
      this.props.uploadActions.clearUploadPolicy();
    } else {
      this.props.uploadActions.getUploadPolicy(this.props.video.id, event.target.files[0]);
    }
  };

  startUpload = () => {
    if (!this.props.upload) {
      return;
    }

    this.props.uploadActions.startUpload(this.props.upload.policy, this.props.upload.file);
  };

  renderHeader() {
    let link = false;
    if (this.props.video) {
      link = <Link className="button" onClick={this.props.uploadActions.clearUploadPolicy} to={`/videos/${this.props.video.id}`}>
        <Icon className="icon__edit" icon="clear" />
      </Link>;
    }

    return <div className="upload__header__container">
      <header className="upload__header">
        Upload Video
      </header>
      <div className="publish__label label__draft">
        Warning: still under development!
      </div>
      {link}
    </div>;
  }

  renderButtons() {
    if (this.props.upload.complete) {
      return <button type="button" className="button__secondary" disabled={true}>
        <Icon icon="done">Uploaded</Icon>
      </button>;
    } else if (this.props.upload.progress) {
      return <div className="upload__progress">
        <progress value={this.props.upload.progress} max={1.0} />
      </div>;
    } else {
      return <button type="button" className="button__secondary" disabled={!this.props.upload.policy} onClick={this.startUpload}>
        <Icon icon="backup">Upload</Icon>
      </button>;
    }
  }

  picker() {
    return <div className="upload__picker">
      <p>File</p>
      <div className="flex-container">
        <input type="file" onChange={this.getUploadPolicy} disabled={this.props.upload.progress} />
        {this.renderButtons()}
      </div>
    </div>;
  }

  render() {
    return <div className="upload">
      <div className="upload__main">
        {this.renderHeader()}

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
