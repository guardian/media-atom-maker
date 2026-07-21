import React from 'react';
import Icon from '../Icon';
import VideoUtils from '../../util/video';
import { Video } from '../../services/VideosApi';
import { AsyncThunk, AsyncThunkConfig } from '@reduxjs/toolkit';
import {
  YouTubeVideoCategory,
  YouTubeChannelWithData
} from '../../services/YoutubeApi';

type Props = {
  video: Video;
  startUpload: AsyncThunk<unknown, any, AsyncThunkConfig>;
  isUploading: boolean;
  categories: YouTubeVideoCategory[];
  channels: YouTubeChannelWithData[];
  saveVideo: any;
};

type State = {
  file: null;
};

export default class YoutubeUpload extends React.Component<Props, State> {
  // @ts-expect-error TS(7018): Object literal's property 'file' implicitly has an... Remove this comment to see the full error message
  state = { file: null };

  setFile = (event: { target: { files: string | any[] } }) => {
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
    const { video, startUpload } = this.props;

    const canUploadToYouTube = VideoUtils.canUploadToYouTube(video);

    return (
      <div className="video__detailbox video__detailbox__assets">
        <div className="form__group">
          <header className="video__detailbox__header video__detailbox__header-with-border">
            Upload to YouTube
          </header>
          <div className="form__row">
            <input
              className="form__field__file"
              type="file"
              // @ts-expect-error TS(2322): Type '(event: { target: { files: string | any[]; }... Remove this comment to see the full error message
              onChange={this.setFile}
              disabled={!canUploadToYouTube || this.props.isUploading}
              accept="video/*,.mxf"
            />
            {!canUploadToYouTube ? (
              <p className="form__message form__message--warning">
                A YouTube channel, category and privacy status are needed before
                uploading a video. Please set these in the YouTube furniture
                tab.
              </p>
            ) : null}
            <button
              type="button"
              className="btn button__secondary__assets"
              disabled={
                !canUploadToYouTube ||
                this.props.isUploading ||
                !this.state.file
              }
              onClick={() =>
                startUpload({
                  id: video.id,
                  file: this.state.file,
                  selfHost: false
                })
              }
            >
              <Icon icon="backup">Upload To YouTube</Icon>
            </button>
          </div>
        </div>
      </div>
    );
  }
}
