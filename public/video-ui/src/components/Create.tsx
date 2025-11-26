import React from 'react';
import {videoPlayerFormats} from "../constants/videoPlayerFormats";
import {blankVideoData} from "../constants/blankVideoData";
import {AppDispatch} from "../util/setupStore";
import {Platform, VideoWithoutId} from "../services/VideosApi";

export default class Create extends React.Component {
  props: React.PropsWithChildren<{
    createVideo: (video: VideoWithoutId) => (dispatch: AppDispatch) => Promise<void>
    inModal: boolean
  }>

  state = {
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

    const videoPlayerFormat = this.state.videoPlayerOption !== "Youtube" ? this.state.videoPlayerOption : undefined;
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
      <>
        <div>
          <label htmlFor={"Headline"}>Headline</label>
          <input
            id={"Headline"}
            name={"headline"}
            onChange={(event) => this.setState({ headline: event.target.value })}
            value={this.state.headline}
            className={'form__field ' + (!this.state.headline ? 'form__field--error' : '')}
          />
        </div>
        <fieldset>
          <div>
            <input
              type="radio"
              id={"Youtube"}
              name="videoPlayerFormat"
              value={"Youtube"}
              checked={this.state.videoPlayerOption === 'Youtube'}
              onChange={() => this.setState({ videoPlayerOption: "Youtube" })}
            />
            <label htmlFor={"Youtube"}>Youtube (off-platform)</label>
          </div>
          {videoPlayerFormats.map((videoPlayerFormat) => (
            <div key={videoPlayerFormat.id}>
              <input
                type="radio"
                id={videoPlayerFormat.id}
                name="videoPlayerFormat"
                value={videoPlayerFormat.id}
                checked={this.state.videoPlayerOption === videoPlayerFormat.id}
                onChange={() => this.setState({ videoPlayerOption: videoPlayerFormat.id })}
              />
              <label htmlFor={videoPlayerFormat.id}>{videoPlayerFormat.title}</label>
            </div>
          ))}
        </fieldset>
        <button className="btn" onClick={this.createVideo} disabled={!this.isFormValid()}>
          Continue
        </button>
      </>
    )
  }
}
