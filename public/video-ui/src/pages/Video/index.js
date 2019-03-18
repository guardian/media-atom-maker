import React from 'react';
import { Link } from 'react-router';
import VideoSelectBar from '../../components/VideoSelectBar/VideoSelectBar';
import VideoPreview from '../../components/VideoPreview/VideoPreview';
import VideoImages from '../../components/VideoImages/VideoImages';
import VideoUsages from '../../components/VideoUsages/VideoUsages';
import Workflow from '../../components/Workflow/Workflow';
import Targeting from '../../components/Targeting/Targeting';
import Icon from '../../components/Icon';
import { formNames } from '../../constants/formNames';
import ReactTooltip from 'react-tooltip';
import { blankVideoData } from '../../constants/blankVideoData';
import { isVideoPublished } from '../../util/isVideoPublished';
import { canonicalVideoPageExists } from '../../util/canonicalVideoPageExists';
import VideoUtils from '../../util/video';
import { Tabs, TabList } from 'react-tabs';
import { FurnitureTab, FurnitureTabPanel } from './tabs/Furniture';
import {
  YoutubeFurnitureTab,
  YoutubeFurnitureTabPanel
} from './tabs/YoutubeFurniture';

class VideoDisplay extends React.Component {
  state = {
    editingFurniture: false,
    editingYoutubeFurniture: false
  };

  componentWillMount() {
    if (this.props.route.mode === 'create') {
      this.props.videoActions.updateVideo(blankVideoData);
      this.props.videoActions.updateVideoEditState(true);
    } else {
      this.props.videoActions.getVideo(this.props.params.id);
      this.props.videoActions.getUsages(this.props.params.id);
    }
  }

  saveAndUpdateVideo = video => {
    if (this.props.route.mode === 'create') {
      this.props.videoActions.createVideo(video)
        .then(() => {
          this.props.videoActions.getUsages(this.props.video.id);
        });
    } else {
      this.props.videoActions.saveVideo(video)
    }
  };

  updateVideo = video => {
    this.props.videoActions.updateVideo(video);
    return Promise.resolve();
  };

  selectVideo = () => {
    window.parent.postMessage({ atomId: this.props.video.id }, '*');
  };

  handleAssetClick = e => {
    if (this.props.videoEditOpen) {
      e.preventDefault();
    }
  };

  renderPreview = () => {
    return (
      <VideoPreview video={this.props.video || {}} />
    );
  };

  renderImages() {
    return (
      <VideoImages
        gridDomain={this.props.config.gridUrl}
        video={this.props.video || {}}
        saveAndUpdateVideo={this.saveAndUpdateVideo}
        videoEditOpen={this.props.videoEditOpen}
        updateErrors={this.props.formErrorActions.updateFormErrors}
      />
    );
  }

  renderPreviewAndImages() {
    const isYoutube = VideoUtils.isYoutube(this.props.video);
    const activeAsset = VideoUtils.getActiveAsset(this.props.video);
    const youtubeAsset = isYoutube && activeAsset;

    return (
      <div className="video__detailbox">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">
            Video Preview
            {youtubeAsset ? ` (${youtubeAsset.id})` : ``}
          </header>
          <Link
            className={'button ' + (this.props.videoEditOpen ? 'disabled' : '')}
            to={`/videos/${this.props.video.id}/upload`}
            onClick={e => this.handleAssetClick(e)}
            data-tip="Edit Assets"
          >
            <Icon className="icon__edit" icon="edit" />
          </Link>
        </div>
        <div className="video-preview">
          {this.renderPreview()}
          {this.renderImages()}
        </div>
      </div>
    );
  }

  renderSelectBar(video) {

    const videoToSelect = isVideoPublished(this.props.video) ?
      this.props.publishedVideo : this.props.video;

    return (
      <VideoSelectBar
        video={videoToSelect}
        onSelectVideo={this.selectVideo}
        embeddedMode={this.props.config.embeddedMode}
      />
    );
  }

  renderUsages() {
    if (this.props.video && this.props.video.id) {
      return (
        <div className="video__detailbox">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">Usages</header>
          </div>
          <VideoUsages
              video={this.props.video || {}}
              publishedVideo={this.props.publishedVideo || {}}
              usages={this.props.usages || {}}
          />
        </div>
      );
    } else {
      return '';
    }
  }

  renderWorkflow() {
    if (this.props.video && this.props.video.id) {
      return (
        <Workflow video={this.props.video}/>
      );
    }
  }

  renderTargeting() {
    return (
      <div className="video__detailbox">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">Suggest this Video</header>
        </div>
        <div className="form__group">
          <Targeting video={this.props.video || {}} />
        </div>
      </div>
    );
  }

  updateEditingState({ key, editing }) {
    this.setState({ [key]: editing });
    this.props.videoActions.updateVideoEditState(editing);
  }

  formHasErrors = formName => {
    const errors = this.props.checkedFormFields[formName]
      ? this.props.checkedFormFields[formName]
      : {};

    return Object.keys(errors).some(field => {
      const value = errors[field];
      return value !== null;
    });
  };

  renderTabs() {
    const {
      videoEditOpen,
      video,
      usages
    } = this.props;

    const {
      editingFurniture,
      editingYoutubeFurniture
    } = this.state;

    const furnitureDisabled = videoEditOpen && editingYoutubeFurniture;
    const ytFurnitureDisabled = videoEditOpen && editingFurniture;

    return (
      <Tabs className="video__detailbox">
        <TabList>
          <FurnitureTab disabled={furnitureDisabled} />
          <YoutubeFurnitureTab disabled={ytFurnitureDisabled} />
        </TabList>
        <FurnitureTabPanel
          editing={editingFurniture}
          onEdit={() =>
            this.updateEditingState({
              key: 'editingFurniture', editing: true
            })
          }
          onCancel={() => {
            this.updateEditingState({
              key: 'editingFurniture', editing: false
            });
            this.getVideo();
          }}
          onSave={() => {
            this.updateEditingState({
              key: 'editingFurniture',
              editing: false
            });
            this.saveAndUpdateVideo(video);
          }}
          canSave={() => !this.formHasErrors(formNames.videoData)}
          video={video}
          updateVideo={this.updateVideo}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
          canonicalVideoPageExists={canonicalVideoPageExists(usages)}
        />
        <YoutubeFurnitureTabPanel
          editing={editingYoutubeFurniture}
          onEdit={() =>
            this.updateEditingState({
              key: 'editingYoutubeFurniture',
              editing: true
            })}
          onCancel={() => {
            this.updateEditingState({
              key: 'editingYoutubeFurniture',
              editing: false
            });
            this.getVideo();
          }}
          onSave={() => {
            this.updateEditingState({
              key: 'editingYoutubeFurniture',
              editing: false
            });
            this.saveAndUpdateVideo(video);
          }}
          canSave={() => !this.formHasErrors(formNames.youtubeFurniture)}
          video={video}
          updateVideo={this.updateVideo}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
        />
      </Tabs>
    );
  }

  render() {
    const video = this.props.video &&
      this.props.params.id === this.props.video.id
      ? this.props.video
      : undefined;

    if (!video) {
      return <div className="container">Loading... </div>;
    }

    return (
      <div>
        {this.renderSelectBar(video)}

        <div className="video">
          <div className="video__main">
            <div className="video__row">
              {this.renderTabs()}
              {this.renderPreviewAndImages()}
            </div>
            <div className="video__row">
              {this.renderTargeting()}
              {this.renderUsages()}
              {this.renderWorkflow()}
            </div>
          </div>
        </div>
        <ReactTooltip />
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as saveVideo from '../../actions/VideoActions/saveVideo';
import * as createVideo from '../../actions/VideoActions/createVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';
import * as videoUsages from '../../actions/VideoActions/videoUsages';
import * as getPublishedVideo
  from '../../actions/VideoActions/getPublishedVideo';
import * as updateVideoEditState
  from '../../actions/VideoActions/updateVideoEditState';
import * as updateFormErrors
  from '../../actions/FormErrorActions/updateFormErrors';
import * as updateFormWarnings
  from '../../actions/FormErrorActions/updateFormWarnings';
import * as videoPageUpdate
  from '../../actions/VideoActions/videoPageUpdate';

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
    videoActions: bindActionCreators(
      Object.assign(
        {},
        getVideo,
        saveVideo,
        createVideo,
        updateVideo,
        videoUsages,
        getPublishedVideo,
        updateVideoEditState,
        videoPageUpdate
      ),
      dispatch
    ),
    formErrorActions: bindActionCreators(
      Object.assign({}, updateFormErrors, updateFormWarnings),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
