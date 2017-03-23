import React from 'react';
import {Link} from 'react-router';
import Icon from '../Icon';
import VideoTrail from './VideoTrail';
import _ from 'lodash';

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

    return <div className="upload__action">
      <label>Asset URL</label>
      <input className="form__field" type="text" placeholder="Paste YouTube URL here" onChange={this.onChange} />
      <button className="btn" type="button" onClick={this.addAsset} disabled={disabled}>Add</button>
    </div>;
  }
}

class VideoUpload extends React.Component {
  state = { file: null };

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

  renderHeader() {
    let link = false;
    if (this.props.video) {
      link = <Link className="button" to={`/videos/${this.props.video.id}`}>
        <Icon className="icon__edit" icon="clear" />
      </Link>;
    }

    return <div className="upload__header">
      <header className="video__detailbox__header">Video Assets</header>
      {link}
    </div>;
  }

  renderButtons(uploading) {
    if (uploading) {
      return false;
    } else {
      return <button type="button" className="button__secondary" disabled={!this.state.file} onClick={this.startUpload}>
        <Icon icon="backup">Upload</Icon>
      </button>;
    }
  }

  renderPicker(uploading) {
    return <div className="upload__action">
      <label>Upload Video</label>
      <input className="form__field" type="file" onChange={this.setFile} disabled={uploading} />
      {this.renderButtons(uploading)}
    </div>;
  }

  render() {
    const uploading = this.props.localUpload.handle !== null || this.props.uploads.length > 0;

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

    return <div className="upload">
      {this.renderHeader()}
      <div className="upload__content">
        <div className="upload__actions">
          {this.renderPicker(uploading)}
          <AddAssetFromURL video={this.props.video} createAsset={this.props.videoActions.createAsset} />
        </div>
        <VideoTrail
          activeVersion={activeVersion}
          assets={assets}
          localUpload={this.props.localUpload}
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
import * as getUpload from '../../actions/UploadActions/getUploads';
import * as localUploadActions from '../../actions/UploadActions/localUpload';
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as revertAsset from '../../actions/VideoActions/revertAsset';

function mapStateToProps(state) {
  return {
    video: state.video,
    localUpload: state.localUpload,
    uploads: state.uploads
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, createAsset, revertAsset), dispatch),
    uploadActions: bindActionCreators(Object.assign({}, localUploadActions, getUpload), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);