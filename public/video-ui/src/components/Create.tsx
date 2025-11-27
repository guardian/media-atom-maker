import React from 'react';
import {videoPlayerFormats} from "../constants/videoPlayerFormats";
import type {VideoPlayerFormat, VideoPlayerOption} from "../constants/videoPlayerFormats";
import {blankVideoData} from "../constants/blankVideoData";
import {AppDispatch} from "../util/setupStore";
import {Platform, VideoWithoutId} from "../services/VideosApi";

export default class Create extends React.Component {
  props: React.PropsWithChildren<{
    createVideo: (video: VideoWithoutId) => (dispatch: AppDispatch) => Promise<void>
    inModal: boolean
    closeCreateModal?: () => void;
  }>

  state: { headline: string; videoPlayerOption: VideoPlayerOption } = {
    headline: "",
    videoPlayerOption: "Youtube",
  };

  closeCreateModal = () => {
    this.setState({ createModalOpen: false });
  };

  openCreateModal = () => {
    this.setState({ createModalOpen: true });
  };

  createVideo = () => {
    const headline = this.state.headline;

    const videoPlayerFormat: VideoPlayerFormat | undefined = this.state.videoPlayerOption !== "Youtube" ? this.state.videoPlayerOption : undefined;
    const platform: Platform = this.state.videoPlayerOption === "Youtube" ? "Youtube" : "Url";

    const videoData = {
      ...blankVideoData,
      title: headline,
      videoPlayerFormat,
      platform
    }

    this.props.createVideo(videoData);
  }

  isFormValid() {
    return !!this.state.headline && !!this.state.videoPlayerOption
  }

  render() {
    return (
      <div className={'create-form ' + (this.props.inModal ? 'create-form-in-modal' : '')}>
        <div>
          <h2 className="create-form__h2">
            Create New Video
          </h2>
          <div className="create-form__headline-container">
            <label className="create-form__label" htmlFor="Headline">Headline</label>
            <input
              id="Headline"
              name="headline"
              onChange={(event) => this.setState({ headline: event.target.value })}
              value={this.state.headline}
              className="form__field"
            />
          </div>
        </div>
        <div>
          <h3 className="create-form__h3">
            Video Player Format
          </h3>
          <div className="create-form__options">
            <div>
              <h4 className="create-form__h4">
                Off Platform
              </h4>
              <div className="create-form__option-row">
                <div className={"create-form__option-container " + (this.state.videoPlayerOption === 'Youtube' ? 'create-form__option-selected' : '')} onClick={() => this.setState({ videoPlayerOption: "Youtube" })}>
                  <div className="create-form__option">
                    <label
                      htmlFor="Youtube"
                      className="create-form__option-radio-label"
                    >
                      Youtube
                    </label>
                    <input
                      type="radio"
                      className="create-form__option-radio-input"
                      id="Youtube"
                      name="videoPlayerFormat"
                      value="Youtube"
                      checked={this.state.videoPlayerOption === 'Youtube'}
                      onChange={() => this.setState({ videoPlayerOption: "Youtube" })}
                    />
                  </div>
                </div>
              </div>
            </div>
            <div>
              <h4 className="create-form__h4">
                Self Hosted
              </h4>
              <div className="create-form__option-row">
                {videoPlayerFormats.map((videoPlayerFormat) => (
                  <div
                    key={videoPlayerFormat.id}
                    className={"create-form__option-container " + (this.state.videoPlayerOption === videoPlayerFormat.id ? 'create-form__option-selected' : '')}
                    onClick={() => this.setState({ videoPlayerOption: videoPlayerFormat.id })}>
                    <div className={"create-form__option"}>
                      <label
                        htmlFor={videoPlayerFormat.id}
                        className="create-form__option-radio-label"
                      >
                        {videoPlayerFormat.title}
                      </label>
                      <input
                        type="radio"
                        className="create-form__option-radio-input"
                        id={videoPlayerFormat.id}
                        name="videoPlayerFormat"
                        value={videoPlayerFormat.id}
                        checked={this.state.videoPlayerOption === videoPlayerFormat.id}
                        onChange={() => this.setState({ videoPlayerOption: videoPlayerFormat.id })}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
        <div className="action-buttons-container">
          {this.props.inModal &&
            <button className="button__secondary" onClick={this.props.closeCreateModal} >
              Cancel
            </button>
          }
          <button className="btn" onClick={this.createVideo} disabled={!this.isFormValid()}>
            Continue
          </button>
        </div>
      </div>
    )
  }
}
