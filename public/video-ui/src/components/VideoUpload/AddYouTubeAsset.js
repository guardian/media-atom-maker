import React from 'react';
import Icon from '../Icon';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import { channelAllowed } from '../../util/channelAllowed';

export default class PlutoProjectPicker extends React.Component {
  state = { file: null };

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

  render() {
    const { video, channels, startUpload } = this.props;

    const isHosted = video.category === 'Hosted';
    const isManaged = channelAllowed(video, channels);

    const hasYouTubeAssets = video.assets.some(
      asset => asset.platform === 'Youtube'
    );

    const editable =
      !hasYouTubeAssets && (!video.channelId || !!video.youtubeCategoryId);

    const missingFields = !video.channelId || !video.youtubeCategoryId;
    const disabled = missingFields || !this.state.file || this.props.uploading;

    if (isHosted || !isManaged) {
      return false;
    }

    return (
      <div className="video__detailbox video__detailbox__assets">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">
            YouTube Video
          </header>
        </div>
        <div className="form__group">
          <ManagedForm
            data={video}
            updateData={this.props.saveVideo}
            editable={editable}
            formName="YouTubeChannel"
          >
            <ManagedField fieldLocation="channelId" name="YouTube Channel">
              <SelectBox selectValues={this.props.channels} />
            </ManagedField>
          </ManagedForm>
          <ManagedForm
            data={video}
            updateData={this.props.saveVideo}
            editable={true}
            formName="YouTubeCategory"
          >
            <ManagedField
              fieldLocation="youtubeCategoryId"
              name="YouTube Category"
            >
              <SelectBox selectValues={this.props.categories} />
            </ManagedField>
            <div>
              <p className="details-list__title">File</p>
              <p className="details-list__field">
                <input
                  className="form__field"
                  type="file"
                  onChange={this.setFile}
                  disabled={this.props.uploading}
                />
              </p>
            </div>
            {missingFields
              ? <div className="error">
                  You have to add a channel and a category before you upload a video.
                </div>
              : <button
                  type="button"
                  className="btn button__secondary__assets"
                  disabled={disabled}
                  onClick={() =>
                    startUpload(
                      video.id,
                      this.state.file,
                      false // not self hosted
                    )}
                >
                  <Icon icon="backup">
                    Upload To YouTube
                  </Icon>
                </button>}
          </ManagedForm>
        </div>
      </div>
    );
  }
}
