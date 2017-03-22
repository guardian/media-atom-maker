import React from 'react';
import {Link} from 'react-router';
import Icon from '../Icon';
import VideoTrail from './VideoTrail';

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
      this.props.uploadActions.startUpload(this.props.video.id, this.state.file);
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

  renderButtons() {
    const uploading = this.props.localUpload.handle !== null;

    if (uploading) {
      return false;
    } else {
      return <button type="button" className="button__secondary" disabled={!this.state.file} onClick={this.startUpload}>
        <Icon icon="backup">Upload</Icon>
      </button>;
    }
  }

  renderPicker() {
    const disabled = this.props.localUpload.handle !== null;

    return <div className="upload__action">
      <label>Upload Video</label>
      <input className="form__field" type="file" onChange={this.setFile} disabled={disabled} />
      {this.renderButtons()}
    </div>;
  }

  renderPlutoProject() {
    return <div className="upload__action">
      <label>Pluto Project</label>
      <select className="form__field form__field--select" disabled>
        <option value="Project">Project</option>
      </select>
    </div>;
  }

  render() {
    const activeVersion = this.props.video ? this.props.video.activeVersion : 0;
    const assets = this.props.video ? this.props.video.assets : [];

    const selectAsset = (assetId, version) => {
      this.props.videoActions.revertAsset(this.props.video.id, assetId, version);
    };

    return <div className="upload">
      {this.renderHeader()}
      <div className="upload__content">
        <div className="upload__actions">
          {this.renderPlutoProject()}
          {this.renderPicker()}
          <AddAssetFromURL video={this.props.video} createAsset={this.props.videoActions.createAsset} />
        </div>
        <VideoTrail
          activeVersion={activeVersion}
          assets={assets}
          selectAsset={selectAsset}
          localUpload={this.props.localUpload}
          uploads={this.props.uploads}
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