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
import { FurnitureTab, FurnitureTabPanel } from './tabs/Furniture';
import {
  YoutubeFurnitureTab,
  YoutubeFurnitureTabPanel
} from './tabs/YoutubeFurniture';
import {ManagementTab, ManagementTabPanel} from "./tabs/Management";
import { WorkflowTab, WorkflowTabPanel } from './tabs/Workflow';
import { UsageTab, UsageTabPanel } from './tabs/Usage';
import { TargetingTab, TargetingTabPanel } from './tabs/Targeting';

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

  saveInWorkflow() {
    const { video } = this.props;
    const { trackedInWorkflow, sections, status: { status, section, note} } = this.props.workflow;
    const { getStatus, trackInWorkflow, updateWorkflowData } = this.props.workflowActions;

    const createWorkflowItem = () => trackInWorkflow({
      video: video,
      section: sections.find(_ => _.id === section),
      status: status,
      note: note
    });

    const updateWorkflowItem = () => updateWorkflowData({
      workflowItem: this.props.workflow.status
    });

    const wfPromise = trackedInWorkflow ? updateWorkflowItem() : createWorkflowItem();

    wfPromise.then(() => getStatus(video));
  }

  updateEditingState({ key, editing }) {
    this.setState({[key]: editing});
    this.props.videoActions.updateVideoEditState(editing);
  }

  renderTabs() {
    const { videoEditOpen, video, workflow, publishedVideo, usages } = this.props;

    const {
      editingFurniture,
      editingYoutubeData,
      editingWorkflow
    } = this.state;

    const furnitureDisabled = videoEditOpen && (editingYoutubeData || editingWorkflow);
    const ytFurnitureDisabled = videoEditOpen && (editingFurniture || editingWorkflow);
    const workflowDisabled = videoEditOpen && (editingFurniture || editingYoutubeData);

    return (
      <Tabs className="video__detailbox">
        <TabList>
          <FurnitureTab disabled={furnitureDisabled}/>
          <YoutubeFurnitureTab disabled={ytFurnitureDisabled}/>
          <WorkflowTab disabled={workflowDisabled} />
          <UsageTab disabled={videoEditOpen} />
          <TargetingTab disabled={videoEditOpen} />
          <ManagementTab disabled={videoEditOpen}/>
        </TabList>
        <FurnitureTabPanel
          editing={editingFurniture}
          onEdit={() => this.updateEditingState({key: 'editingFurniture', editing: true})}
          onCancel={() => this.updateEditingState({key: 'editingFurniture', editing: false})}
          onSave={() => {
            this.updateEditingState({key: 'editingFurniture', editing: false});
            this.saveAndUpdateVideo(video);
          }}
          video={video}
          updateVideo={this.updateVideo}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
          canonicalVideoPageExists={canonicalVideoPageExists(this.props.usages)}
        />
        <YoutubeFurnitureTabPanel
          editing={editingYoutubeData}
          onEdit={() => this.updateEditingState({key: 'editingYoutubeData', editing: true})}
          onCancel={() => this.updateEditingState({key: 'editingYoutubeData', editing: false})}
          onSave={() => {
            this.updateEditingState({key: 'editingYoutubeData', editing: false});
            this.saveAndUpdateVideo(video);
          }}
          video={video}
          updateVideo={this.updateVideo}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
        />
        <WorkflowTabPanel
          editing={editingWorkflow}
          onEdit={() => this.updateEditingState({key: 'editingWorkflow', editing: true})}
          onCancel={() => this.updateEditingState({key: 'editingWorkflow', editing: false})}
          onSave={() => {
            this.updateEditingState({key: 'editingWorkflow', editing: false});
            this.saveInWorkflow();
          }}
          video={video}
          workflow={workflow}
        />
        <UsageTabPanel
          video={video}
          publishedVideo={publishedVideo || {}}
          usages={usages || {}}
        />
        <TargetingTabPanel video={video} />
        <ManagementTabPanel video={video} updateVideo={this.updateVideo}/>
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
import * as getStatus from '../../actions/WorkflowActions/getStatus';
import * as trackInWorkflow from '../../actions/WorkflowActions/trackInWorkflow';
import * as updateWorkflowData from '../../actions/WorkflowActions/updateWorkflowData';

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
    ),
    workflowActions: bindActionCreators(
      Object.assign({}, getStatus, trackInWorkflow, updateWorkflowData),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
