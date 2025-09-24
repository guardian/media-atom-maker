import React from 'react';
import { Link } from 'react-router';
import VideoSelectBar from '../../components/VideoSelectBar/VideoSelectBar';
import VideoPreview from '../../components/VideoPreview/VideoPreview';
import VideoImages from '../../components/VideoImages/VideoImages';
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
import { WorkflowTab, WorkflowTabPanel } from './tabs/Workflow';
import { UsageTab, UsageTabPanel } from './tabs/Usage';
import { TargetingTab, TargetingTabPanel } from './tabs/Targeting';
import { ManagementTab, ManagementTabPanel } from './tabs/Management';
import { PlutoTab, PlutoTabPanel } from './tabs/Pluto';

class VideoDisplay extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      isCreateMode: props.route.props?.mode === 'create',
      editingFurniture: false,
      editingYoutubeFurniture: false,
      editingWorkflow: false
    };

    if (this.state.isCreateMode) {
      this.props.videoActions.updateVideo(blankVideoData);
      this.updateEditingState({ key: 'editingFurniture', editing: true });
    } else {
      this.getVideo();
      this.getWorkflowState();
      this.getUsages();
    }
  }

  getVideo() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  getWorkflowState() {
    this.props.workflowActions.getStatus(this.props.video);
  }

  getUsages() {
    this.props.videoActions.fetchUsages(this.props.params.id);
  }

  saveAndUpdateVideo = video => {
    const { isCreateMode } = this.state;

    if (isCreateMode) {
      return this.props.videoActions.createVideo(video).then(() => {
        this.setState({ isCreateMode: false });
        this.props.videoActions.fetchUsages(this.props.video.id);
      });
    } else {
      return this.props.videoActions.saveVideo(video);
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
        updateVideo={this.updateVideo}
        saveAndUpdateVideo={this.saveAndUpdateVideo}
        videoEditOpen={this.props.videoEditOpen}
        updateErrors={this.props.formErrorActions.updateFormErrors}
      />
    );
  }

  renderDescription(){
    return (
      <div>
        <p className="video__images_heading">How are these images used?</p>
        <div>
          <table className="video__images_description_table">
            <thead>
              <tr>
                <th scope="row">Guardian Video Thumbnail Image</th>
                <th scope="row">Composer Trail Image</th>
                <th scope="row">Youtube Video Thumbnail Image</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>Used as the preview image for a video in articles and on video pages.</td>
                <td>Used as an article trail image when specified. (Otherwise Main Image is used).</td>
                <td>
                  Used on YouTube when specified. (Otherwise Main Image is used).
                  <br/>
                  The atom must be published for the override to take effect. Changes can take up to fifteen minutes to apply.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
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
            <h3>Video Preview</h3>
            {youtubeAsset &&  (
              <p className= "video-asset">
                <span className= "video-asset-number">Asset {activeAsset.version}:</span>
                <span>({youtubeAsset.id})</span>
              </p>
            )
            }
          </header>
          <asset-handle data-source="mam"
                        data-source-type="video"
                        data-thumbnail={this.props.video?.posterImage?.assets?.[0]?.file}
                        data-external-url={VideoUtils.isYoutube(this.props.video) && getYouTubeEmbedUrl(VideoUtils.getActiveAsset(this.props.video)?.id)}
                        data-embeddable-url={window.location.href}>
          </asset-handle>
          <Link
            className={'button ' + (this.props.videoEditOpen ? 'disabled' : '')}
            to={`/videos/${this.props.video.id}/upload`}
            onClick={e => this.handleAssetClick(e)}
            data-tip="Edit Assets"
          >
            <Icon className={"icon__edit"  + (this.props.videoEditOpen ? ' icon__edit__disabled' : '')} icon="edit" />
          </Link>
        </div>
        <div className="video-preview">
          {this.renderPreview()}
          {this.renderImages()}
          {this.renderDescription()}
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

    const {
      sections,
      status: { status, section, note, isTrackedInWorkflow, prodOffice, priority }
    } = this.props.workflow;

    const {
      getStatus,
      trackInWorkflow,
      updateWorkflowData
    } = this.props.workflowActions;

    const createWorkflowItem = () =>
      trackInWorkflow({
        video: video,
        section: sections.find(_ => _.id === section),
        status: status,
        note: note,
        prodOffice: prodOffice,
        priority: priority
      });

    const updateWorkflowItem = () =>
      updateWorkflowData({
        workflowItem: this.props.workflow.status
      });

    const wfPromise = isTrackedInWorkflow
      ? updateWorkflowItem()
      : createWorkflowItem();

    return wfPromise.then(() => getStatus(video));
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
      usages,
      workflow,
      publishedVideo,
      saveState
    } = this.props;

    const { saving } = this.props.saveState;

    const {
      isCreateMode,
      editingFurniture,
      editingYoutubeFurniture,
      editingWorkflow
    } = this.state;

    const furnitureDisabled = videoEditOpen && (editingYoutubeFurniture || editingWorkflow);
    const ytFurnitureDisabled = videoEditOpen && (editingFurniture || editingWorkflow);
    const workflowDisabled = videoEditOpen && (editingFurniture || editingYoutubeFurniture);

    return (
      <Tabs className="video__detailbox">
        <TabList>
          <FurnitureTab disabled={furnitureDisabled} />
          <YoutubeFurnitureTab disabled={ytFurnitureDisabled} />
          <WorkflowTab disabled={workflowDisabled || isCreateMode} />
          <UsageTab disabled={videoEditOpen || isCreateMode} />
          <TargetingTab disabled={videoEditOpen || isCreateMode} />
          <ManagementTab disabled={videoEditOpen || isCreateMode} />
          <PlutoTab disabled={videoEditOpen || isCreateMode} />
        </TabList>
        <FurnitureTabPanel
          editing={editingFurniture}
          onEdit={() =>
            this.updateEditingState({
              key: 'editingFurniture', editing: true
            })
          }
          onCancel={() => {
            !isCreateMode && this.updateEditingState({
              key: 'editingFurniture', editing: false
            });
            this.getVideo();
          }}
          onSave={() => {
            this.saveAndUpdateVideo(video)
              .then(() => {
                this.updateEditingState({
                  key: 'editingFurniture',
                  editing: false
                });
              })
              .catch(() => {
                // Keep editing state as true on save failure
                // Error handling is done in the saveVideo action
              });
          }}
          canSave={() => !this.formHasErrors(formNames.videoData) && !saving}
          canCancel={() => !isCreateMode && !saving}
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
            this.saveAndUpdateVideo(video)
              .then(() => {
                this.updateEditingState({
                  key: 'editingYoutubeFurniture',
                  editing: false
                });
              })
              .catch(() => {
                // Keep editing state as true on save failure
                // Error handling is done in the saveVideo action
              });
          }}
          canSave={() => !this.formHasErrors(formNames.youtubeFurniture) && !saving}
          canCancel={() => !saving}
          video={video}
          updateVideo={this.updateVideo}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
        />
        <WorkflowTabPanel
          editing={editingWorkflow}
          onEdit={() =>
            this.updateEditingState({ key: 'editingWorkflow', editing: true })}
          onCancel={() => {
            this.updateEditingState({ key: 'editingWorkflow', editing: false });
            this.getWorkflowState();
          }}
          onSave={() => {
            this.saveInWorkflow()
              .then(() => {
                this.updateEditingState({ key: 'editingWorkflow', editing: false });
              })
              .catch(() => {
                // Keep editing state as true on save failure
                // Error handling should be implemented in workflow actions
              });
          }}
          canSave={() => workflow.status.section && workflow.status.status && !saving}
          canCancel={() => !saving}
          video={video}
          isTrackedInWorkflow={workflow.status.isTrackedInWorkflow || false}
        />
        <UsageTabPanel
          video={video}
          publishedVideo={publishedVideo || {}}
          usages={usages || {}}
        />
        <TargetingTabPanel video={video} />
        <ManagementTabPanel video={video} updateVideo={this.updateVideo} />
        <PlutoTabPanel video={video} />
      </Tabs>
    );
  }

  render() {
    const video = this.props.video &&
      (this.props.params.id === this.props.video.id || this.state.isCreateMode)
      ? this.props.video
      : undefined;

    if (!video) {
      return <div className="container">Loading... </div>;
    }

    return (
      <div>
        {this.renderSelectBar(video)}
        <pinboard-preselect data-composer-id={getComposerId() ?? "unknown"} data-tool="media-atom-maker"></pinboard-preselect>

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
import { fetchUsages } from '../../slices/usage';
import * as getPublishedVideo
  from '../../actions/VideoActions/getPublishedVideo';
import * as videoPageUpdate
  from '../../actions/VideoActions/videoPageUpdate';
import * as getStatus from '../../actions/WorkflowActions/getStatus';
import * as trackInWorkflow
  from '../../actions/WorkflowActions/trackInWorkflow';
import * as updateWorkflowData
  from '../../actions/WorkflowActions/updateWorkflowData';
import {getYouTubeEmbedUrl} from "../../components/utils/YouTubeEmbed";
import {getComposerId} from "../../util/getComposerData";
import {updateFormWarnings} from "../../slices/formFieldsWarning";
import {updateVideoEditState} from "../../slices/editState";
import {updateFormErrors} from "../../slices/checkedFormFields";
import {selectPublishedVideo, selectVideo} from "../../slices/video";

function mapStateToProps(state) {
  return {
    video: selectVideo(state),
    config: state.config,
    usages: state.usage,
    publishedVideo: selectPublishedVideo(state),
    videoEditOpen: state.videoEditOpen,
    checkedFormFields: state.checkedFormFields,
    workflow: state.workflow,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(
      {
        updateVideoEditState,
        fetchUsages,
        ...getVideo,
        ...saveVideo,
        ...createVideo,
        ...updateVideo,
        ...getPublishedVideo,
        ...videoPageUpdate
        },
      dispatch
    ),
    formErrorActions: bindActionCreators(
      {updateFormErrors, updateFormWarnings},
      dispatch
    ),
    workflowActions: bindActionCreators(
      Object.assign({}, getStatus, trackInWorkflow, updateWorkflowData),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
