import React from 'react';
import CheckBox from '../FormFields/CheckBox';
import Icon from '../Icon';

export default class UploadPicker extends React.Component {
  state = { file: null };

  videoDataMissing = () => {
    return !this.props.video.channelId || !this.props.video.youtubeCategoryId;
  };

  renderDataMissingMessage() {
    if (this.state.file && this.videoDataMissing()) {
      return (
        <div className="error">
          {' '}
          'You have to add a channel and a category before you upload a video. '
        </div>
      );
    }
    return null;
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

  startUpload = () => {
    if (this.props.video && this.state.file) {
      this.props.uploadActions.startUpload(
        this.props.video.id,
        this.state.file,
        this.props.canUploadToYouTube ? this.state.selfHost : true
      );

      this.setState({ selfHost: false });
    }
  };

  renderButtons(uploading) {
    const metadataCorrect = !this.videoDataMissing() || this.state.selfHost;
    const enabled = this.state.file && metadataCorrect;

    if (uploading) {
      return false;
    } else {
      return (
        <div>
          <button
            type="button"
            className="btn button__secondary__assets"
            disabled={!enabled}
            onClick={() => this.startUpload()}
          >
            <Icon icon="backup">Upload</Icon>
          </button>

          {this.props.canSelfHost
            ? <CheckBox
                fieldName="Options"
                fieldDetails="Host on YouTube"
                editable={this.props.canUploadToYouTube}
                fieldValue={
                  this.props.canUploadToYouTube ? !this.state.selfHost : false
                }
                onUpdateField={v => this.setState({ selfHost: !v })}
              />
            : false}
        </div>
      );
    }
  }

  render() {
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
            disabled={this.props.uploading}
          />
          {this.renderDataMissingMessage()}
          {this.renderButtons(this.props.uploading)}
        </div>
      </div>
    );
  }
}
