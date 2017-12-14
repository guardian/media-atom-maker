import React from 'react';
import Icon from '../Icon';
import VideoUtils from '../../util/video';

export default class YoutubeUpload extends React.Component {
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

  renderWarning() {
    return (
      <span className="form__message form__message--warning">
        A YouTube channel, category and privacy status are needed before uploading a video
      </span>
    );
  }

  render() {
    const { video, startUpload } = this.props;

    const canUploadToYouTube = VideoUtils.canUploadToYouTube(video);

    return (
      <div className="video__detailbox video__detailbox__assets">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">
            Upload to YouTube
          </header>
        </div>
        <div className="form__group">
          {!canUploadToYouTube ? this.renderWarning() : ''}
          <input
            className="form__field"
            type="file"
            onChange={this.setFile}
            disabled={!canUploadToYouTube || this.props.uploading}
            accept="video/*,.mxf"
          />
          <button
            type="button"
            className="btn button__secondary__assets"
            disabled={!canUploadToYouTube || this.props.uploading || !this.state.file}
            onClick={() =>
              startUpload({
                id: video.id,
                file: this.state.file,
                selfHost: false
              })}
          >
            <Icon icon="backup">
              Upload To YouTube
            </Icon>
          </button>
        </div>
      </div>
    );
  }
}
