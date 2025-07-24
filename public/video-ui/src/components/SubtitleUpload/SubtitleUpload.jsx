import React from 'react';
import Icon from '../Icon';
import PropTypes from 'prop-types';

/**
 * Just a temporary UI for testing subtitle uploading
 */
export default class SubtitleUpload extends React.Component {
  state = {
    file: null,
    isValid: null,
    uploading: null,
    uploaded: null,
    error: null
  };

  static propTypes = {
    startUpload: PropTypes.func.isRequired,
    video: PropTypes.object.isRequired
  };

  validate(files) {
    if (files.length === 1) {
      const file = files[0];
      const reader = new FileReader();

      reader.onload = e => {
        const content = e.target.result;

        this.setState({
          file: file,
          isValid: true,
          uploaded: false,
          uploading: false
        });
      };

      reader.readAsText(file);
    }
  }

  uploadFile() {
    const file = this.state.file;
    const { id, activeVersion } = this.props.video;
    const version = activeVersion; // temp for test ui

    this.setState({ uploading: true });
    console.log("uploading "+id+"-"+version+"------"+JSON.stringify(this.props.video));
    this.props.startUpload({id, version, file})
      .then(_ => this.setState({uploading: false, uploaded: true}))
      .catch(e => this.setState({uploading: false, uploaded: false, error: e}));
  }

  getUploadButtonText() {
    if (!this.state.file) {
      return 'No file chosen '+JSON.stringify(this.props.video);
    }

    if (this.state.uploaded) {
      return 'Uploaded '+JSON.stringify(this.props.video);
    }

    if (this.state.isValid) {
      return 'Upload';
    }

    if (!this.state.isValid) {
      return 'Invalid file';
    }
  }

  getButtonClassName() {
    const base = 'btn';

    if (this.state.uploaded) {
      return `${base} ${base}--complete`
    }

    if (this.state.uploading) {
      return `${base} ${base}--loading`;
    }

    return base;
  }

  getButtonIcon() {
    return this.state.uploaded ? 'done' : 'file_upload'
  }

  render() {
    return (
      <section className="video__detailbox">
        <div className="form__group">
          <header className="video__detailbox__header video__detailbox__header-with-border">
            Upload Subtitle File
          </header>
          <div className="form__row">
            <input type="file"
                  className="form__field__file"
                  accept=".srt;.vtt"
                  disabled={this.state.uploading}
                  onChange={e => this.validate(e.target.files)}/>
            <button type="button"
                    className={this.getButtonClassName()}
                    disabled={this.state.uploading || !this.state.isValid || this.state.uploaded}
                    onClick={() => this.uploadFile()}
            >
              <Icon icon={this.getButtonIcon()}>
                {this.getUploadButtonText()}
              </Icon>
            </button>
          </div>
        </div>
      </section>
    );
  }
}
