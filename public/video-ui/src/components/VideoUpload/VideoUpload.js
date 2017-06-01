import React from 'react';
import Icon from '../Icon';
import VideoTrail from './VideoTrail';
import { getStore } from '../../util/storeAccessor';
import VideoYoutube from '../VideoYoutube/VideoYoutube';
import { formNames } from '../../constants/formNames';

class AddAssetFromURL extends React.Component {
  constructor(props) {
    super(props);
    this.state = { uri: null };
  }

  addAsset = () => {
    if (this.state.uri) {
      this.props.createAsset(this.state, this.props.video);
    }
  };

  onChange = e => {
    this.setState({ uri: e.target.value });
  };

  render() {
    const disabled = !this.state.uri;

    return (
      <div>
        <div className="video__detailbox">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">Asset URL</header>
          </div>
          <div className="form__group">
            <div className="form__row">
              <div>
                <input
                  className="form__field"
                  type="text"
                  placeholder="Paste YouTube URL here"
                  onChange={this.onChange}
                />
                <button
                  className="btn"
                  type="button"
                  onClick={this.addAsset}
                  disabled={disabled}
                >
                  Add
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

class VideoUpload extends React.Component {
  state = {
    file: null,
    videoEditOpen: false
  };

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  setFile = event => {
    if (!this.props.video) {
      return;
    }

    if (event.target.files.length == 0) {
      this.setState({ file: null });
    } else {
      this.setState({ file: event.target.files[0] });
    }
  };

  startUpload = selfHost => {
    if (this.props.video && this.state.file) {
      this.props.uploadActions.startUpload(
        this.props.video.id,
        this.state.file,
        selfHost
      );
    }
  };

  renderButtons(uploading) {
    if (uploading) {
      return false;
    } else {
      return (
        <div>
          {' '}
          {this.renderStartUpload(false, 'Upload')}
          {' '}
          {getStore().getState().config.permissions.addSelfHostedAsset
            ? this.renderStartUpload(true, 'Upload avoiding YouTube')
            : null}
        </div>
      );
    }
  }

  renderStartUpload(selfHost, msg) {
    return (
      <div>
        <button
          type="button"
          className="btn button__secondary__assets"
          disabled={!this.state.file}
          onClick={() => this.startUpload(selfHost)}
        >
          <Icon icon="backup">{msg}</Icon>
        </button>
      </div>
    );
  }

  renderUpload(uploading) {
    // the permissions are also validated on the server-side for each request

    return (
      <div className="video__detailbox upload__action">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">Upload Video</header>
        </div>
        <input
          className="form__field"
          type="file"
          onChange={this.setFile}
          disabled={uploading}
        />
        {this.renderButtons(uploading)}
      </div>
    );
  }

  setEditingState = state => {
    if (!state) {
      this.props.videoActions.saveVideo(this.props.video);
    }

    this.setState({
      videoEditOpen: state
    });
  };

  renderActions(uploading) {
    return (
      <div className="upload__actions upload__actions--non-empty">
        {this.renderUpload(uploading)}
        <AddAssetFromURL
          video={this.props.video}
          createAsset={this.props.videoActions.createAsset}
        />
      </div>
    );
  }

  renderEditButton = () => {
    if (this.props && this.state.videoEditOpen) {
      return (
        <button onClick={() => this.setEditingState(false)}>
          <Icon className="icon__done" icon="done" />
        </button>
      );
    } else {
      return (
        <button onClick={() => this.setEditingState(true)}>
          <Icon className="icon__edit" icon="edit" />
        </button>
      );
    }
  };

  render() {
    const uploading = this.props.s3Upload.total > 0;

    const activeVersion = this.props.video ? this.props.video.activeVersion : 0;
    const assets = this.props.video ? this.props.video.assets : [];

    const selectAsset = (assetId, version) => {
      this.props.videoActions.revertAsset(
        this.props.video.id,
        assetId,
        version
      );
    };

    return (
      <div>
        <div className="video__detailbox">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">YouTube Data</header>
          </div>
          {this.renderEditButton()}
          <VideoYoutube
            video={this.props.video || {}}
            updateVideo={this.props.videoActions.updateVideo}
            editable={this.state.videoEditOpen}
            formName={formNames.youtube}
            updateErrors={this.props.formErrorActions.updateFormErrors}
          />
        </div>
        <div className="video__main">
          <div className="video__main__header">
            {this.renderActions(uploading)}
            <VideoTrail
              activeVersion={activeVersion}
              assets={assets}
              s3Upload={this.props.s3Upload}
              uploads={this.props.uploads}
              selectAsset={selectAsset}
              getVideo={() =>
                this.props.videoActions.getVideo(this.props.video.id)}
              getUploads={() =>
                this.props.uploadActions.getUploads(this.props.video.id)}
            />
          </div>
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';
import * as saveVideo from '../../actions/VideoActions/saveVideo';
import * as getUpload from '../../actions/UploadActions/getUploads';
import * as s3UploadActions from '../../actions/UploadActions/s3Upload';
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as revertAsset from '../../actions/VideoActions/revertAsset';
import * as updateFormErrors
  from '../../actions/FormErrorActions/updateFormErrors';

function mapStateToProps(state) {
  return {
    video: state.video,
    s3Upload: state.s3Upload,
    uploads: state.uploads
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(
      Object.assign(
        {},
        getVideo,
        updateVideo,
        saveVideo,
        createAsset,
        revertAsset
      ),
      dispatch
    ),
    uploadActions: bindActionCreators(
      Object.assign({}, s3UploadActions, getUpload),
      dispatch
    ),
    formErrorActions: bindActionCreators(
      Object.assign({}, updateFormErrors),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);
