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
    const { video, permissions, uploading, startUpload } = this.props;

    if (!permissions || !permissions.addSelfHostedAsset) {
      return false;
    }

    return (
      <div className="video__detailbox video__detailbox__assets">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">
            Self-Hosted Video
          </header>
        </div>
        <div className="form__group">
          <input
            className="form__field"
            type="file"
            onChange={this.setFile}
            disabled={uploading}
          />
          <button
            type="button"
            className="btn button__secondary__assets"
            disabled={!this.state.file || uploading}
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
    );
  }
}
