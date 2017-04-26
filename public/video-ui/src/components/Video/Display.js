import React from 'react';
import {Link} from 'react-router';
import VideoSelectBar from '../VideoSelectBar/VideoSelectBar';
import VideoPreview from '../VideoPreview/VideoPreview';
import VideoUsages from '../VideoUsages/VideoUsages';
import VideoMetaData from '../VideoMetaData/VideoMetaData';
import YoutubeMetaData from '../YoutubeMetaData/YoutubeMetaData';
import VideoData from '../VideoData/VideoData';
import VideoPoster from '../VideoPoster/VideoPoster';
import AdvancedActions from '../Videos/AdvancedActions';
import GridImageSelect from '../utils/GridImageSelect';
import {getVideoBlock} from '../../util/getVideoBlock';
import {getStore} from '../../util/storeAccessor';
import Icon from '../Icon';
import {formNames} from '../../constants/formNames';
import {blankVideoData} from '../../constants/blankVideoData';
import FieldNotification from '../../constants/FieldNotification';

class VideoDisplay extends React.Component {

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
    this.props.videoActions.getUsages(this.props.params.id);
  }

  componentWillUnmount() {
    this.props.videoActions.updateVideo(blankVideoData);
  }

  saveVideo = () => {
    this.props.videoActions.saveVideo(this.props.video);
  }

  saveAndUpdateVideoPoster = (poster) => {
    const newVideo = Object.assign({}, this.props.video, {
      posterImage: poster
    });
    this.saveAndUpdateVideo(newVideo);

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

  manageEditingState = () => {

    if (this.props.videoEditOpen) {

      this.saveAndUpdateVideo(this.props.video);
    }

    this.props.videoActions.updateVideoEditState(!this.props.videoEditOpen);
  };

  cannotEditStatus = () => {
    return this.props.video.expiryDate <= Date.now();
  };

  pageCreate = () => {

    this.setState({
      pageCreated: true
    });

    const metadata = {
      title: this.props.video.title,
      standfirst: this.props.video.description
    };

    const videoBlock = getVideoBlock(this.props.video.id, metadata);

    return this.props.videoActions.createVideoPage(this.props.video.id, metadata, this.getComposerUrl(), videoBlock);
  }

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  }

  videoPageUsages = () => {
      const filterUsageType = this.props.usages.filter(value => value.type === 'video');

      if(filterUsageType.length === 0){
        return (
          <button
            className="button__secondary"
            disabled={this.props.videoEditOpen}
            onClick={this.pageCreate}>
              <Icon icon="add_to_queue"></Icon> Create Video Page
          </button>
        );
      }
  }

  cannotCloseEditForm = () => {

    const formName = formNames.videoData;

    const errors = this.props.checkedFormFields[formName] ? this.props.checkedFormFields[formName] : {};
    return Object.keys(errors).some(field => {
      const value = errors[field];
      return value !== null;
    });

  };

  validateDescription = (description) => {
    if (!description) {
      return new FieldNotification('required', 'It is recommeded you fill in this field for seo', FieldNotification.warning);
    }
    return null;
  }

  handleAssetClick = (e) => {
    if (this.props.videoEditOpen) {
      e.preventDefault();
    }
  }

  renderEditButton = () => {

    if (this.props && this.props.videoEditOpen) {
      return (
        <button disabled={this.cannotCloseEditForm()} onClick={() => this.manageEditingState()}>
          <Icon className={"icon__done " + (this.cannotCloseEditForm() ? "disabled": "")} icon="done" />
        </button>
      );
    } else {
      return (
        <button disabled={this.props.videoEditOpen} onClick={() => this.manageEditingState()}>
          <Icon className={"icon__edit " + (this.props.videoEditOpen ? "disabled" : "")} icon="edit" />
        </button>
      );
    }
  }

  renderPreview = () => {
    return <div className="video__detailbox">
      <div className="video__detailbox__header__container">
        <header className="video__detailbox__header">Video Preview</header>
        <Link className={"button " + (this.props.videoEditOpen ? "disabled" : "")} to={`/videos/${this.props.video.id}/upload`}
            onClick={e => this.handleAssetClick(e)}>
          <Icon className="icon__edit video__temporary_button" icon="edit">
            Edit Assets
          </Icon>
        </Link>
      </div>
      <VideoPreview video={this.props.video || {}} />
    </div>;
  };

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
              {this.renderPreview()}
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Video Data</header>
                  {this.renderEditButton()}
                </div>
                <VideoData
                  video={this.props.video || {}}
                  updateVideo={this.updateVideo}
                  editable={this.props.videoEditOpen}
                  formName={formNames.videoData}
                  updateErrors={this.props.formErrorActions.updateFormErrors}
                />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Poster Image</header>
                  <GridImageSelect
                    updateVideo={this.saveAndUpdateVideoPoster}
                    gridUrl={this.props.config.gridUrl}
                    disabled={this.props.videoEditOpen}
                    createMode={false}/>
                </div>
                <VideoPoster
                  video={this.props.video || {}}
                  updateVideo={this.saveAndUpdateVideo}
                  formName={formNames.posterImage}
                  updateErrors={this.props.formErrorActions.updateFormErrors}
                />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Usages</header>
                  {this.videoPageUsages()}
                </div>
                <VideoUsages
                  video={this.props.video || {}}
                  publishedVideo={this.props.publishedVideo || {}}
                  usages={this.props.usages || []}
                />
              </div>
              <div className="video__detailbox">
                <AdvancedActions video={this.props.video || {}} />
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
import * as updateFormErrors from '../../actions/FormErrorActions/updateFormErrors';

function mapStateToProps(state) {
  return {
    video: state.video,
    config: state.config,
    usages: state.usage,
    composerPageWithUsage: state.pageCreate,
    publishedVideo: state.publishedVideo,
    videoEditOpen: state.videoEditOpen,
    checkedFormFields: state.checkedFormFields
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, saveVideo, updateVideo, videoUsages, videoPageCreate, getPublishedVideo, updateVideoEditState), dispatch),
    formErrorActions: bindActionCreators(Object.assign({}, updateFormErrors), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
