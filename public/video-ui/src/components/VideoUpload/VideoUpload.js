import React from 'react';
import Icon from '../Icon';
import VideoTrail from './VideoTrail';
import {getStore} from '../../util/storeAccessor';
import _ from 'lodash';
import {blankVideoData} from '../../constants/blankVideoData';

class AddAssetFromURL extends React.Component {
  constructor(props) {
    super(props);
    this.state = { uri: null };
  }

  addAsset = () => {
    if(this.state.uri) {
      this.props.createAsset(this.state, this.props.video);
    }
  };

  onChange = (e) => {
    this.setState({ uri: e.target.value });
  }

  render() {
    const disabled = !this.state.uri;

    return <div className="video__detailbox">
      <div className="video__detailbox__header__container">
        <header className="video__detailbox__header">Asset URL</header>
      </div>
      <div className="form__group">
      <div className="form__row">
        <div>
          <input className="form__field" type="text" placeholder="Paste YouTube URL here" onChange={this.onChange} />
          <button className="btn" type="button" onClick={this.addAsset} disabled={disabled}>Add</button>
        </div>
      </div>
    </div>
    </div>;
  }
}

class VideoUpload extends React.Component {
  state = { file: null };

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  componentWillUnmount() {
    this.props.videoActions.updateVideo(blankVideoData);
  }

  setFile = (event) => {
    if (!this.props.video) {
      return;
    }

    if (event.target.files.length == 0) {
      this.setState({ file: null });
    } else {
      this.setState({ file: event.target.files[0] });
    }
  };

  startUpload = () => {
    if(this.props.video && this.state.file) {
      const atomId = this.props.video.id;

      this.props.uploadActions.startUpload(atomId, this.state.file, () => {
        // on complete
        this.props.uploadActions.getUploads(atomId);
      });
    }
  };

  renderButtons(uploading) {
    if (uploading) {
      return false;
    } else {
      return <button type="button" className="btn button__secondary__assets" disabled={!this.state.file} onClick={this.startUpload}>
        <Icon icon="backup">Upload</Icon>
      </button>;
    }
  }

  renderActions(uploading) {
    // the permissions are also validated on the server-side for each request
    if(!getStore().getState().config.permissions.addAsset) {
      return false;
    }

    return <div>
      <div className="video__detailbox upload__action">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">Upload Video</header>
        </div>
          <input className="form__field" type="file" onChange={this.setFile} disabled={uploading} />
          {this.renderButtons(uploading)}
      </div>
      <AddAssetFromURL video={this.props.video} createAsset={this.props.videoActions.createAsset} />
    </div>;
  }

  render() {
    const uploading = this.props.s3Upload.handle !== null;

    const activeVersion = this.props.video ? this.props.video.activeVersion : 0;
    const assets = this.props.video ? this.props.video.assets : [];

    const selectAsset = (assetId, version) => {
      this.props.videoActions.revertAsset(this.props.video.id, assetId, version);
    };

    // We want to display uploads that do not yet have a corresponding asset in the atom.
    // VideoTrail will poll appropriately.
    const uploads = _.filter(this.props.uploads, (upload) => {
        const version = upload.metadata.pluto.assetVersion;
        const exists = _.some(assets, (asset) => asset.version === version);
        return !exists;
    });

    return <div className="video__main">
      <div className="video__main__header">
        <div className="upload__actions">
          {this.renderActions(uploading)}
        </div>
        <VideoTrail
          activeVersion={activeVersion}
          assets={assets}
          s3Upload={this.props.s3Upload}
          uploads={uploads}
          selectAsset={selectAsset}
          getVideo={() => this.props.videoActions.getVideo(this.props.video.id)}
          getUploads={() => this.props.uploadActions.getUploads(this.props.video.id)}
        />
      </div>
    </div>;
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';
import * as getUpload from '../../actions/UploadActions/getUploads';
import * as s3UploadActions from '../../actions/UploadActions/s3Upload';
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as revertAsset from '../../actions/VideoActions/revertAsset';

function mapStateToProps(state) {
  return {
    video: state.video,
    s3Upload: state.s3Upload,
    uploads: state.uploads
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, updateVideo, createAsset, revertAsset), dispatch),
    uploadActions: bindActionCreators(Object.assign({}, s3UploadActions, getUpload), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);
