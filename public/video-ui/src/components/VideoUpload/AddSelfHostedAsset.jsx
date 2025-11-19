import React from 'react';
import Icon from '../Icon';

export default class AddSelfHostedAsset extends React.Component {
  state = { file: null };

  setFile = event => {
    if (!this.props.video) {
      return;
    }

    if (event.target.files.length === 0) {
      this.setState({ file: null });
    } else {
      this.setState({ file: event.target.files[0] });
    }
  };

  render() {
    const { video, permissions, isUploading, startUpload } = this.props;

    if (!permissions || !permissions.addSelfHostedAsset) {
      return false;
    }

    const canUpload = video.videoPlayerOption !== undefined || !permissions.videoPlayerOption;

    return (
      <div className="video__detailbox video__detailbox__assets">
        <div className="form__group">
          <header className="video__detailbox__header video__detailbox__header-with-border">
            Self-Hosted Video
          </header>
          <div className="form__row">
            <input
              className="form__field__file"
              type="file"
              onChange={this.setFile}
              disabled={!canUpload || isUploading}
              accept="video/*,.mxf"
            />
            { !canUpload ?
              <p className="form__message form__message--warning">
                A Video Player Format is needed before uploading a self-hosted video. Please set this in the Furniture tab.
              </p> : null
            }
            <button
              type="button"
              className="btn button__secondary__assets"
              disabled={!this.state.file || isUploading}
              onClick={() =>
                startUpload({
                  id: video.id,
                  file: this.state.file,
                  selfHost: true
                })}
            >
              <Icon icon="backup">
                Upload
              </Icon>
            </button>
          </div>
        </div>
      </div>
    );
  }
}
