import React, {createRef} from 'react';
import {VideoCreateOptionDetails, videoCreateOptions} from "../constants/videoCreateOptions";
import type {VideoPlayerFormat, VideoCreateOption} from "../constants/videoCreateOptions";
import {blankVideoData} from "../constants/blankVideoData";
import {AppDispatch} from "../util/setupStore";
import {Platform, VideoWithoutId} from "../services/VideosApi";
import Checkmark from "../../images/checkmark.svg?react";
import Cross from "../../images/cross.svg?react";
import Info from "../../images/info.svg?react";
import Loop from "../../images/loop.svg?react";
import Youtube from "../../images/youtube.svg?react";
import Cinemagraph from "../../images/cinemagraph.svg?react";
import NonYoutube from "../../images/nonyoutube.svg?react";

export default class Create extends React.Component {
  props: React.PropsWithChildren<{
    createVideo: (video: VideoWithoutId) => (dispatch: AppDispatch) => Promise<void>
    inModal: boolean
    closeCreateModal?: () => void;
  }>;

  state: { title: string; videoCreateOption: VideoCreateOption } = {
    title: "",
    videoCreateOption: "Youtube"
  };

  createVideo = () => {
    const title = this.state.title;

    const videoPlayerFormat: VideoPlayerFormat | undefined = this.state.videoCreateOption !== "Youtube" ? this.state.videoCreateOption : undefined;
    const platform: Platform = this.state.videoCreateOption === "Youtube" ? "Youtube" : "Url";

    const videoData = {
      ...blankVideoData,
      title,
      videoPlayerFormat,
      platform
    };

    this.props.createVideo(videoData);
  };

  isFormValid() {
    return !!this.state.title && !!this.state.videoCreateOption;
  }

  iconMap = {
    Youtube: <Youtube/>,
    Loop: <Loop/>,
    Cinemagraph: <Cinemagraph/>,
    Default: <NonYoutube/>
  };

  renderVideoCreateOption(videoCreateOptionDetails: VideoCreateOptionDetails) {
    const isSelected = this.state.videoCreateOption === videoCreateOptionDetails.id;
    const inputRef = createRef<HTMLInputElement>();

    return (
      <div
        key={videoCreateOptionDetails.id}
        className={
          "create-form__option " +
          (isSelected ? 'create-form__option--selected ' : '')
        }
        onClick={() => {
          this.setState({ videoCreateOption: videoCreateOptionDetails.id });
          inputRef?.current?.focus();
        }}>
        <div className="create-form__option-controls">
          <div className="create-form__option-radio-summary">
            <div className="create-form__option-radio-label-and-icon">
              <div className="create-form__option-radio-icon">
                {this.iconMap[videoCreateOptionDetails.id]}
              </div>
              <label
                htmlFor={videoCreateOptionDetails.id}
                className="create-form__option-radio-label"
              >
                {videoCreateOptionDetails.title}
              </label>
            </div>
            <div className="create-form__option-radio-description">
              {videoCreateOptionDetails.description}
            </div>
          </div>
          <input
            type="radio"
            className="create-form__option-radio"
            id={videoCreateOptionDetails.id}
            name="videoPlayerFormat"
            value={videoCreateOptionDetails.id}
            checked={isSelected}
            onChange={() => this.setState({ videoCreateOption: videoCreateOptionDetails.id })}
            ref={inputRef}
          />
        </div>
        {
          <div className={
            "create-form__option-specifications " +
            (isSelected ? 'create-form__option-specifications--visible' : '')
          }>
            <ul aria-label="positives">
              {videoCreateOptionDetails.specifications.positive.map(positiveSpecification => (
                <li key={positiveSpecification} className="create-form__list-item--specification">
                  {positiveSpecification}
                  <Checkmark/>
                </li>
              ))}
            </ul>
            <ul aria-label="negatives">
              {videoCreateOptionDetails.specifications.negative.map(negativeSpecifiation => (
                <li key={negativeSpecifiation} className="create-form__list-item--specification">
                  {negativeSpecifiation}
                  <Cross/>
                </li>
              ))}
            </ul>
            <ul aria-label="other information">
              {videoCreateOptionDetails.specifications.info.map(infoSpecification => (
                <li key={infoSpecification} className="create-form__list-item--specification">
                  {infoSpecification}
                  <Info/>
                </li>
              ))}
            </ul>
          </div>
        }
      </div>
    );
  }

  render() {
    return (
      <div className={'create-form ' + (this.props.inModal ? 'create-form--modal' : '')}>
        <h2 className="create-form__heading--level-2">
          Create New Video
        </h2>
        <div className="create-form__contents">
          <div className="create-form__title">
            <label className="create-form__label" htmlFor="title">Title</label>
            <input
              id="title"
              name="Title"
              onChange={(event) => this.setState({ title: event.target.value })}
              value={this.state.title}
              className="form__field"
            />
          </div>
          <div>
            <h3 className="create-form__heading--level-3">
              Video Player Format
            </h3>
            <div className="create-form__options">
              <div>
                  {videoCreateOptions.offPlatform.map((videoCreateOptionDetails) => (
                    this.renderVideoCreateOption(videoCreateOptionDetails)
                  ))}
                  {videoCreateOptions.selfHosted.map((videoCreateOptionDetails) => (
                    this.renderVideoCreateOption(videoCreateOptionDetails)
                  ))}
              </div>
            </div>
          </div>
          <div className="create-form__action-buttons-outer">
            {!this.state.title &&
              <div className="create-form__action-buttons--validation-warning">
                You need to add a Title to create a video.
              </div>
            }
            <div className="create-form__action-buttons">
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
        </div>
      </div>
    );
  }
}
