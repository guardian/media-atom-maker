import React from 'react';
import {Link} from 'react-router';
import Icon from '../Icon';

class VideoUpload extends React.Component {
  constructor(props) {
    super(props);
    this.state = { file: null };
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
    const complete = this.props.upload.handle !== null && this.props.upload.progress === this.props.upload.total;

    if (complete) {
      return <button type="button" className="button__secondary" disabled={true}>
        <Icon icon="done">Uploaded</Icon>
      </button>;
    } else if (this.props.upload.progress) {
      return <div className="upload__progress">
        <progress value={this.props.upload.progress} max={this.props.upload.total} />
      </div>;
    } else {
      return <button type="button" className="button__secondary" disabled={!this.state.file} onClick={this.startUpload}>
        <Icon icon="backup">Upload</Icon>
      </button>;
    }
  }

  renderPicker() {
    return <div className="upload__action">
      <label>Upload Video</label>
      <input className="form__field" type="file" onChange={this.setFile} disabled={this.props.upload.progress} />
      {this.renderButtons()}
    </div>;
  }

  renderAddAsset() {
    return <div className="upload__action">
      <label>Asset URL</label>
      <input className="form__field" type="text" placeholder="Paste YouTube URL here" />
      <button className="btn" type="button">Save</button>
    </div>;
  }

  renderPlutoProject() {
    return <div className="upload__action">
      <label>Pluto Project</label>
      <select className="form__field form__field--select" disabled>
        <option value="Project">Project</option>
      </select>
      <button className="btn" type="button">Save</button>
    </div>;
  }

  render() {
    return <div className="upload">
      {this.renderHeader()}
      <div className="upload__content">
        <div className="upload__actions">
          {this.renderPlutoProject()}
          {this.renderAddAsset()}
          {this.renderPicker()}
        </div>
        <div className="upload__assets">ASSETS</div>
      </div>
    </div>;
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as uploadActions from '../../actions/UploadActions/upload';

function mapStateToProps(state) {
  return {
    video: state.video,
    upload: state.upload
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo), dispatch),
    uploadActions: bindActionCreators(Object.assign({}, uploadActions), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);