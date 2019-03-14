import React from 'react';
import { Link } from 'react-router';
import VideoSelectBar from '../../components/VideoSelectBar/VideoSelectBar';
import VideoPreview from '../../components/VideoPreview/VideoPreview';
import VideoImages from '../../components/VideoImages/VideoImages';
import Icon from '../../components/Icon';
import ReactTooltip from 'react-tooltip';
import { blankVideoData } from '../../constants/blankVideoData';
import { canonicalVideoPageExists } from '../../util/canonicalVideoPageExists';
import { isVideoPublished } from '../../util/isVideoPublished';
import VideoUtils from '../../util/video';
import { Tabs, TabList } from 'react-tabs';
import 'react-tabs/style/react-tabs.css';
import { FurnitureTab, FurnitureTabPanel } from './tabs/Furniture';
import { WorkflowTab, WorkflowTabPanel } from './tabs/Workflow';
import {
  YoutubeFurnitureTab,
  YoutubeFurnitureTabPanel
} from './tabs/YoutubeFurniture';
import { UsageTab, UsageTabPanel } from './tabs/Usage';
import { TargetingTab, TargetingTabPanel } from './tabs/Targeting';
import { ManagementTab, ManagementTabPanel } from './tabs/Management';

class VideoDisplay extends React.Component {
  state = {
    editingFurniture: false,
    editingYoutubeData: false,
    editingWorkflow: false
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
      this.props.videoActions.createVideo(video).then(() => {
        this.props.videoActions.getUsages(this.props.video.id);
      });
    } else {
      this.props.videoActions.saveVideo(video);
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
    return <VideoPreview video={this.props.video || {}} />;
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

  renderSelectBar({ video, publishedVideo, config: { embeddedMode } }) {
    const videoToSelect = isVideoPublished(video) ? publishedVideo : video;

    return (
      <VideoSelectBar
        video={videoToSelect}
        onSelectVideo={this.selectVideo}
        embeddedMode={embeddedMode}
      />
    );
  }

  updateEditingState(stateKey, editing, saving = false) {
    this.setState({ [stateKey]: editing });
    this.props.videoActions.updateVideoEditState(editing);

    if (saving) {
      this.saveAndUpdateVideo(this.props.video);
    }
  }

  renderTabs() {
    const { videoEditOpen } = this.props;

    const {
      editingFurniture,
      editingYoutubeData,
      editingWorkflow
    } = this.state;

    const furnitureDisabled =
      videoEditOpen && (editingYoutubeData || editingWorkflow);

    const ytFurnitureDisabled =
      videoEditOpen && (editingFurniture || editingWorkflow);

    const workflowDisabled =
      videoEditOpen && (editingFurniture || editingYoutubeData);

    return (
      <Tabs className="video__detailbox">
        <TabList>
          <FurnitureTab disabled={furnitureDisabled} />
          <YoutubeFurnitureTab disabled={ytFurnitureDisabled} />
          <WorkflowTab disabled={workflowDisabled} />
          <UsageTab disabled={videoEditOpen} />
          <TargetingTab disabled={videoEditOpen} />
          <ManagementTab disabled={videoEditOpen} />
        </TabList>

        <FurnitureTabPanel
          editing={editingFurniture}
          onEdit={() => this.updateEditingState('editingFurniture', true)}
          onCancel={() => this.updateEditingState('editingFurniture', false)}
          onSave={() =>
            this.updateEditingState('editingFurniture', false, true)}
          video={this.props.video}
          updateVideo={this.updateVideo}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
          canonicalVideoPageExists={canonicalVideoPageExists(this.props.usages)}
        />
        <YoutubeFurnitureTabPanel
          editing={editingYoutubeData}
          onEdit={() => this.updateEditingState('editingYoutubeData', true)}
          onCancel={() => this.updateEditingState('editingYoutubeData', false)}
          onSave={() =>
            this.updateEditingState('editingYoutubeData', false, true)}
          video={this.props.video}
          updateVideo={this.updateVideo}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
        />
        <WorkflowTabPanel
          editing={editingWorkflow}
          onEdit={() => this.updateEditingState('editingWorkflow', true)}
          onCancel={() => this.updateEditingState('editingWorkflow', false)}
          onSave={() => this.updateEditingState('editingWorkflow', false, true)}
          video={this.props.video}
          workflow={this.props.workflow}
        />
        <UsageTabPanel
          video={this.props.video || {}}
          publishedVideo={this.props.publishedVideo || {}}
          usages={this.props.usages || {}}
        />
        <TargetingTabPanel video={this.props.video} />
        <ManagementTabPanel
          video={this.props.video}
          updateVideo={this.updateVideo}
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
        {this.renderSelectBar(this.props)}

        <div className="video">
          <div className="video__main">
            <div className="video__row">
              {this.renderTabs()}
              {this.renderPreviewAndImages()}
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
import * as videoPageUpdate from '../../actions/VideoActions/videoPageUpdate';

function mapStateToProps(state) {
  return {
    video: state.video,
    config: state.config,
    usages: state.usage,
    composerPageWithUsage: state.pageCreate,
    publishedVideo: state.publishedVideo,
    videoEditOpen: state.videoEditOpen,
    checkedFormFields: state.checkedFormFields,
    workflow: state.workflow
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
