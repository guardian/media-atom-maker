import React from 'react';
import Icon from '../Icon';
import { Video } from '../../services/VideosApi';
import { AsyncThunk, AsyncThunkConfig } from '@reduxjs/toolkit';

type Props = {
  video: Video;
  isUploading: boolean;
  startUpload: AsyncThunk<unknown, any, AsyncThunkConfig>;
};

type State = {
  file: null;
};

export default class AddSelfHostedAsset extends React.Component<Props, State> {
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
    const { video, isUploading, startUpload } = this.props;

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
              // @ts-expect-error TS(2322): Type '(event: { target: { files: string | any[]; }... Remove this comment to see the full error message
              onChange={this.setFile}
              disabled={isUploading}
              accept="video/*,.mxf"
            />
            <button
              type="button"
              className="btn button__secondary__assets"
              disabled={!this.state.file || isUploading}
              onClick={() =>
                startUpload({
                  id: video.id,
                  file: this.state.file,
                  selfHost: true
                })
              }
            >
              <Icon icon="backup">Upload</Icon>
            </button>
          </div>
        </div>
      </div>
    );
  }
}
