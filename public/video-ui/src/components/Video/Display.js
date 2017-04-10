import React from 'react';
import {Link} from 'react-router';
import VideoSelectBar from '../VideoSelectBar/VideoSelectBar';
import VideoPreview from '../VideoPreview/VideoPreview';
import VideoUsages from '../VideoUsages/VideoUsages';
import VideoMetaData from '../VideoMetaData/VideoMetaData';
import YoutubeMetaData from '../YoutubeMetaData/YoutubeMetaData';
import VideoPoster from '../VideoPoster/VideoPoster';
import AdvancedActions from '../Videos/AdvancedActions';
import GridImageSelect from '../utils/GridImageSelect';
import {getVideoBlock} from '../../util/getVideoBlock';
import {getStore} from '../../util/storeAccessor';
import Icon from '../Icon';
import {formNames} from '../../constants/formNames';
import {blankVideoData} from '../../constants/blankVideoData';

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
          <button className="button__secondary" onClick={this.pageCreate}><Icon icon="add_to_queue"></Icon> Create Video Page</button>
        );
      }
  }

  cannotOpenEditForm = () => {
    if (this.props.editState && (this.props.editState.metadataEditable || this.props.editState.youtubeEditable)) {
      return true;
    }

    return false;
  }


  cannotCloseEditForm = (property) => {

    let formName;
    if (property === 'metadataEditable') {
      formName = formNames.metadata;
    } else if (property === 'youtubeEditable') {
      formName = formNames.youtube;
    }
    const errors = this.props.checkedFormFields[formName] ? this.props.checkedFormFields[formName] : {};
    return Object.keys(errors).some(field => {
      const value = errors[field];
      return value.length !== 0;
    });

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

  renderPreview = () => {
    return <div className="video__detailbox">
      <div className="video__detailbox__header__container">
        <header className="video__detailbox__header">Video Preview</header>
        <Link className="button" to={`/videos/${this.props.video.id}/upload`}>
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
                  <header className="video__detailbox__header">Video Meta Data</header>
                  {this.renderEditButton('metadataEditable')}
                </div>

                <VideoMetaData
                  video={this.props.video || {}}
                  updateVideo={this.updateVideo}
                  editable={this.props.editState.metadataEditable}
                  formName={formNames.metadata}
                  updateErrors={this.props.formErrorActions.updateFormErrors}
                 />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Youtube Meta Data</header>
                  {this.renderEditButton('youtubeEditable')}
                </div>
                <YoutubeMetaData
                  video={this.props.video || {}}
                  updateVideo={this.updateVideo}
                  editable={this.props.editState.youtubeEditable}
                  formName={formNames.youtube}
                  updateErrors={this.props.formErrorActions.updateFormErrors}
                />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Poster Image</header>
                  <GridImageSelect
                    editState={this.props.editState}
                    updateVideo={this.saveAndUpdateVideoPoster}
                    gridUrl={this.props.config.gridUrl}
                    disabled={this.props.editState.youtubeEditable || this.props.editState.metadataEditable}
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
    editState: state.editState,
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
