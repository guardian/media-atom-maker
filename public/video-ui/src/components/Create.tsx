import React from 'react';
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
import Standard from "../../images/standard.svg?react";

export default class Create extends React.Component {
  props: React.PropsWithChildren<{
    createVideo: (video: VideoWithoutId) => (dispatch: AppDispatch) => Promise<void>
    inModal: boolean
    closeCreateModal?: () => void;
  }>;

  state: { headline: string; videoCreateOption: VideoCreateOption } = {
    headline: "",
    videoCreateOption: "Youtube"
  };

  closeCreateModal = () => {
    this.setState({ createModalOpen: false });
  };

  openCreateModal = () => {
    this.setState({ createModalOpen: true });
  };

  createVideo = () => {
    const headline = this.state.headline;

    const videoPlayerFormat: VideoPlayerFormat | undefined = this.state.videoCreateOption !== "Youtube" ? this.state.videoCreateOption : undefined;
    const platform: Platform = this.state.videoCreateOption === "Youtube" ? "Youtube" : "Url";

    const videoData = {
      ...blankVideoData,
      title: headline,
      videoPlayerFormat,
      platform
    };

    this.props.createVideo(videoData);
  };

  isFormValid() {
    return !!this.state.headline && !!this.state.videoCreateOption;
  }

  iconMap = {
    Youtube: <Youtube/>,
    Loop: <Loop/>,
    Cinemagraph: <Cinemagraph/>,
    Default: <Standard/>
  };

  renderVideoCreateOption(videoCreateOptionDetails: VideoCreateOptionDetails, isGroupSelected: boolean) {
    const isSelected = this.state.videoCreateOption === videoCreateOptionDetails.id;

    return (
      <div
        key={videoCreateOptionDetails.id}
        className={
          "create-form__option-container " +
          (isSelected ? 'create-form__option-selected ' : '') +
          (isGroupSelected ? 'create-form__option-group-selected' : '')
        }
        onClick={() => this.setState({ videoCreateOption: videoCreateOptionDetails.id })}>
        <div className="create-form__option">
          <div className="create-form__option-radio-label-and-icon">
            {this.iconMap[videoCreateOptionDetails.id]}
            <label
              htmlFor={videoCreateOptionDetails.id}
              className="create-form__option-radio-label"
            >
              {videoCreateOptionDetails.title}
            </label>
          </div>
          <input
            type="radio"
            className="create-form__option-radio-input"
            id={videoCreateOptionDetails.id}
            name="videoPlayerFormat"
            value={videoCreateOptionDetails.id}
            checked={isSelected}
            onChange={() => this.setState({ videoCreateOption: videoCreateOptionDetails.id })}
          />
        </div>
        { isGroupSelected &&
          <div className="create-form__option-specifications">
            <ul aria-label="positives">
              {videoCreateOptionDetails.specifications.positive.map(positiveSpecification => (
                <li key={positiveSpecification} className="create-form__list-item__specification">
                  {positiveSpecification}
                  <Checkmark/>
                </li>
              ))}
            </ul>
            <ul aria-label="negatives">
              {videoCreateOptionDetails.specifications.negative.map(negativeSpecifiation => (
                <li key={negativeSpecifiation} className="create-form__list-item__specification">
                  {negativeSpecifiation}
                  <Cross/>
                </li>
              ))}
            </ul>
            <ul aria-label="other information">
              {videoCreateOptionDetails.specifications.info.map(infoSpecification => (
                <li key={infoSpecification} className="create-form__list-item__specification">
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
                {videoCreateOptions.offPlatform.map((videoCreateOptionDetails) => (
                  this.renderVideoCreateOption(videoCreateOptionDetails, this.state.videoCreateOption === "Youtube")
                ))}
              </div>
            </div>
            <div>
              <h4 className="create-form__h4">
                Self Hosted
              </h4>
              <div className="create-form__option-row">
                {videoCreateOptions.selfHosted.map((videoCreateOptionDetails) => (
                  this.renderVideoCreateOption(videoCreateOptionDetails, this.state.videoCreateOption !== "Youtube")
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
    );
  }
}
