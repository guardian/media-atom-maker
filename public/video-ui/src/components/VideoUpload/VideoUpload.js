import React from 'react';
import Icon from '../Icon';
import VideoTrail from './VideoTrail';
import { getStore } from '../../util/storeAccessor';
import YoutubeMetadata from '../YoutubeMetadata/YoutubeMetadata';

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
        <div className="video__detailbox video__detailbox__assets">
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
  hasCategories = () =>
    this.props &&
    this.props.youtube &&
    this.props.youtube.categories.length !== 0;
  hasChannels = () =>
    this.props.youtube && this.props.youtube.channels.length !== 0;
  hasPlutoProjects = () =>
    this.props.pluto && this.props.pluto.projects.length !== 0;

  state = {
    file: null,
    videoEditOpen: false
  };

  // We only require a pluto id when pluto projects are available
  shouldAddPlutoId = () => {
    return (
      !(this.props.video.plutoData && this.props.video.plutoData.projectId) &&
      this.hasPlutoProjects()
    );
  };

  videoDataMissing = () => {
    return (
      this.shouldAddPlutoId() ||
      !this.props.video.channelId ||
      !this.props.video.youtubeCategoryId
    );
  };

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
    if (!this.hasPlutoProjects()) {
      this.props.plutoActions.getProjects();
    }
    if (!this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
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

  renderDataMissingMessage() {
    if (this.state.file && this.videoDataMissing()) {
      const dataInstructions =
        'You have to add a channel, category  ' +
        (this.hasPlutoProjects()
          ? 'and a pluto id before uploading to youtube.'
          : 'before uploading to youtube. Pluto is currently unavailable. You can add a pluto project id later.');

      return <div className="error"> {dataInstructions} </div>;
    }
    return null;
  }

  renderStartUpload(selfHost, msg) {
    return (
      <div>
        <button
          type="button"
          className="btn button__secondary__assets"
          disabled={!this.state.file || (!selfHost && this.videoDataMissing())}
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
      <div className="video__detailbox video__detailbox__assets upload__action">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">Upload Video</header>
        </div>
        <div className="form__group">
          <input
            className="form__field"
            type="file"
            onChange={this.setFile}
            disabled={uploading}
          />
          {this.renderDataMissingMessage()}
          {this.renderButtons(uploading)}
        </div>
      </div>
    );
  }

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

  renderYouTubeData = () => {
    return (
      <div>
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">
            YouTube Data
          </header>
        </div>
        <YoutubeMetadata
          video={this.props.video || {}}
          saveVideo={this.props.videoActions.saveVideo}
          youtube={this.props.youtube}
          editable={this.shouldAddPlutoId()}
          pluto={this.props.pluto}
        />
      </div>
    );
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
        <div className="video__main">
          <div className="video__main__header">
            <div className="video__detailbox">
              {this.renderYouTubeData()}
              {this.renderActions(uploading)}
            </div>
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
import * as saveVideo from '../../actions/VideoActions/saveVideo';
import * as getUpload from '../../actions/UploadActions/getUploads';
import * as s3UploadActions from '../../actions/UploadActions/s3Upload';
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as revertAsset from '../../actions/VideoActions/revertAsset';
import * as getProjects from '../../actions/PlutoActions/getProjects';
import * as getCategories from '../../actions/YoutubeActions/getCategories';
import * as getChannels from '../../actions/YoutubeActions/getChannels';

function mapStateToProps(state) {
  return {
    video: state.video,
    s3Upload: state.s3Upload,
    uploads: state.uploads,
    pluto: state.pluto,
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(
      Object.assign({}, getVideo, saveVideo, createAsset, revertAsset),
      dispatch
    ),
    uploadActions: bindActionCreators(
      Object.assign({}, s3UploadActions, getUpload),
      dispatch
    ),
    youtubeActions: bindActionCreators(
      Object.assign({}, getCategories, getChannels),
      dispatch
    ),
    plutoActions: bindActionCreators(Object.assign({}, getProjects), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);
