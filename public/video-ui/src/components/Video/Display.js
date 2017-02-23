import React from 'react';
import VideoAssets from '../VideoAssets/VideoAssets';
import VideoSelectBar from '../VideoSelectBar/VideoSelectBar';
import VideoPreview from '../VideoPreview/VideoPreview';
import VideoUsages from '../VideoUsages/VideoUsages';
import VideoMetaData from '../VideoMetaData/VideoMetaData';
import YoutubeMetaData from '../YoutubeMetaData/YoutubeMetaData';
import VideoPoster from '../VideoPoster/VideoPoster';
import GridImageSelect from '../utils/GridImageSelect';
import Icon from '../Icon';
import {validate} from '../../constants/videoEditValidation';

class VideoDisplay extends React.Component {

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  saveVideo = () => {
    this.props.videoActions.saveVideo(this.props.video);
  }

  saveAndUpdateVideo = (video) => {
    this.props.videoActions.saveVideo(video);
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  selectVideo = () => {
    window.parent.postMessage({atomId: this.props.video.id}, '*');
  };

  manageEditingState = (property) => {

    if (this.props.editState[property]) {

      this.saveAndUpdateVideo(this.props.video);
    }

    const newEditableState = Object.assign(this.props.editState, {[property]: !this.props.editState[property]});
    this.props.videoActions.updateVideoEditState(newEditableState);
  };

  cannotEditStatus = () => {
    return this.props.video.expiryDate <= Date.now();
  };

  cannotOpenEditForm = () => {
    if (this.props.editState && (this.props.editState.metadataEditable || this.props.editState.youtubeEditable)) {
      return true;
    }

    return false;
  }


  cannotCloseEditForm = (property) => {

    const formFields = [];
    if (property === 'metadataEditable') {
      formFields.splice(0, 0, 'title', 'category');
    } else if (property === 'youtubeEditable') {
      formFields.splice(0, 0, 'youtubeCategory', 'youtubeChannel', 'privacyStatus');
    }
    const errors  = validate(Object.assign(this.props.video, {
      youtubeCategory: this.props.video.youtubeCategoryId,
      youtubeChannel: this.props.video.channelId
    }));

    if (formFields.some(field => {
      return Object.keys(errors).includes(field);
    })) {
      return true;
    }
    return false;

  };

  renderEditButton = (property) => {

    if (this.props && this.props.editState[property]) {
      return (
        <button disabled={this.cannotCloseEditForm(property)} onClick={() => this.manageEditingState(property)}>
          <Icon className={"icon__done " + (this.cannotCloseEditForm(property) ? "disabled": "")} icon="done" />
        </button>
      );
    } else {
      return (
        <button disabled={this.cannotOpenEditForm()} onClick={() => this.manageEditingState(property)}>
          <Icon className={"icon__edit " + (this.cannotOpenEditForm() ? "disabled" : "")} icon="edit" />
        </button>
      );
    }
  }

  render() {
    const video = this.props.video && this.props.params.id === this.props.video.id ? this.props.video : undefined;

    if (!video) {
      return <div className="container">Loading... </div>;
    }

    return (
      <div>
        <VideoSelectBar video={video} onSelectVideo={this.selectVideo} publishedVideo={this.props.publishedVideo} embeddedMode={this.props.config.embeddedMode} />

        <div className="video">
          <div className="video__main">
            <div className="video__main__header">
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Preview</header>
                </div>
                <VideoPreview video={this.props.video || {}} />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Video Meta Data</header>
                  {this.renderEditButton('metadataEditable')}
                </div>
                <VideoMetaData
                  component={VideoMetaData}
                  video={this.props.video || {}}
                  updateVideo={this.updateVideo}
                  editable={this.props.editState.metadataEditable}
                 />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Youtube Meta Data</header>
                  {this.renderEditButton('youtubeEditable')}
                </div>
                <YoutubeMetaData
                  component={YoutubeMetaData}
                  video={this.props.video || {}}
                  saveVideo={this.saveVideo}
                  updateVideo={this.updateVideo}
                  disableStatusEditing={this.cannotEditStatus()}
                  editable={this.props.editState.youtubeEditable}
                />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Poster Image</header>
                  <GridImageSelect editState={this.props.editState} video={this.props.video || {}} updateVideo={this.saveAndUpdateVideo} gridUrl={this.props.config.gridUrl} createMode={false}/>
                </div>
                <VideoPoster
                  video={this.props.video || {}}
                  updateVideo={this.saveAndUpdateVideo}
                />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Usages</header>
                </div>
                <VideoUsages
                  video={this.props.video || {}}
                  publishedVideo={this.props.publishedVideo || {}}
                  fetchUsages={this.props.videoActions.getUsages}
                  usages={this.props.usages || []}
                  createComposerPage={this.props.videoActions.createVideoPage}
                />
              </div>
              <div className="video__detailbox">
                <VideoAssets video={this.props.video || {}} />
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as saveVideo from '../../actions/VideoActions/saveVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';
import * as videoUsages from '../../actions/VideoActions/videoUsages';
import * as videoPageCreate from '../../actions/VideoActions/videoPageCreate';
import * as getPublishedVideo from '../../actions/VideoActions/getPublishedVideo';
import * as updateVideoEditState from '../../actions/VideoActions/updateVideoEditState';

function mapStateToProps(state) {
  return {
    video: state.video,
    config: state.config,
    usages: state.usage,
    composerPageWithUsage: state.pageCreate,
    publishedVideo: state.publishedVideo,
    editState: state.editState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, saveVideo, updateVideo, videoUsages, videoPageCreate, getPublishedVideo, updateVideoEditState), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
