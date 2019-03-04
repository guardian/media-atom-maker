import React from 'react';
import { Link } from 'react-router';
import VideoSelectBar from '../../components/VideoSelectBar/VideoSelectBar';
import VideoPreview from '../../components/VideoPreview/VideoPreview';
import VideoImages from '../../components/VideoImages/VideoImages';
import VideoUsages from '../../components/VideoUsages/VideoUsages';
import VideoData from '../../components/VideoData/VideoData';
import YoutubeData from '../../components/YoutubeData';
import Workflow from '../../components/Workflow/Workflow';
import Targeting from '../../components/Targeting/Targeting';
import Icon from '../../components/Icon';
import { formNames } from '../../constants/formNames';
import FieldNotification from '../../constants/FieldNotification';
import ReactTooltip from 'react-tooltip';
import { blankVideoData } from '../../constants/blankVideoData';
import KeywordsApi from '../../services/KeywordsApi';
import YouTubeKeywords from '../../constants/youTubeKeywords';
import { getYouTubeTagCharCount } from '../../util/getYouTubeTagCharCount';
import { canonicalVideoPageExists } from '../../util/canonicalVideoPageExists';
import { isVideoPublished } from '../../util/isVideoPublished';
import VideoUtils from '../../util/video';
import ContentChangeDetails from '../../components/ContentChangeDetails';
import Flags from '../../components/Flags';
import DurationReset from '../../components/DurationReset';
import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import 'react-tabs/style/react-tabs.css';

class VideoDisplay extends React.Component {
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

  composerKeywordsToYouTube = () => {
    return Promise.all(
      this.props.video.keywords.map(keyword =>
        KeywordsApi.composerTagToYouTube(keyword)
      )
    ).then(youTubeKeywords => {
      const oldTags = this.props.video.tags;
      const keywordsToCopy = youTubeKeywords.reduce((tagsAdded, keyword) => {
        const allAddedTags = oldTags.concat(tagsAdded);
        if (
          keyword !== '' &&
          allAddedTags.every(oldTag => oldTag !== keyword)
        ) {
          tagsAdded.push(keyword);
        }
        return tagsAdded;
      }, []);
      const newVideo = Object.assign({}, this.props.video, {
        tags: oldTags.concat(keywordsToCopy)
      });

      this.updateVideo(newVideo);
    });
  };

  selectVideo = () => {
    window.parent.postMessage({ atomId: this.props.video.id }, '*');
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

  cannotCloseEditForm = () => {
    const formName = formNames.videoData;

    const errors = this.props.checkedFormFields[formName]
      ? this.props.checkedFormFields[formName]
      : {};
    return Object.keys(errors).some(field => {
      const value = errors[field];
      return value !== null;
    });
  };

  validateKeywords = keywords => {
    if (
      !Array.isArray(keywords) ||
      keywords.length === 0 ||
      keywords.every(keyword => {
        return keyword.match(/^tone/);
      })
    ) {
      if (canonicalVideoPageExists(this.props.usages)) {
        return new FieldNotification(
          'error',
          'A series or a keyword tag is required for updating composer pages',
          FieldNotification.error
        );
      }
      return new FieldNotification(
        'desired',
        'A series or a keyword tag is required for creating composer pages',
        FieldNotification.warning
      );
    }
    return null;
  };

  validateYouTubeKeywords = youTubeKeywords => {
    const charLimit = YouTubeKeywords.maxCharacters;
    const numberOfChars = getYouTubeTagCharCount(youTubeKeywords);

    if (numberOfChars > charLimit) {
      return new FieldNotification(
        'required',
        `Maximum characters allowed in YouTube keywords is ${charLimit}.`,
        FieldNotification.error
      );
    }

    return null;
  };

  handleAssetClick = e => {
    if (this.props.videoEditOpen) {
      e.preventDefault();
    }
  };

  renderVideoDataEditButton = () => {
    if (this.props && this.props.videoEditOpen) {
      return (
        <button
          disabled={this.cannotCloseEditForm()}
          onClick={() => this.manageEditingState()}
        >
          <Icon
            className={
              'icon__done ' + (this.cannotCloseEditForm() ? 'disabled' : '')
            }
            icon="save"
          >
            Save changes
          </Icon>
        </button>
      );
    } else {
      return (
        <button
          disabled={this.props.videoEditOpen}
          onClick={() => this.manageEditingState()}
        >
          <Icon
            className={
              'icon__edit ' + (this.props.videoEditOpen ? 'disabled' : '')
            }
            icon="edit"
          />
        </button>
      );
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

  renderSelectBar(video) {
    const videoToSelect = isVideoPublished(this.props.video)
      ? this.props.publishedVideo
      : this.props.video;

    return (
      <VideoSelectBar
        video={videoToSelect}
        onSelectVideo={this.selectVideo}
        embeddedMode={this.props.config.embeddedMode}
      />
    );
  }

  renderUsages({ video, publishedVideo, usages }) {
    if (video.id) {
      return (
        <VideoUsages
          video={video || {}}
          publishedVideo={publishedVideo || {}}
          usages={usages || {}}
        />
      );
    } else {
      return '';
    }
  }

  renderWorkflow({ video }) {
    if (video.id) {
      return <Workflow video={video} />;
    }
  }

  renderTargeting({ video }) {
    if (video.id) {
      return <Targeting video={video} />;
    }
  }

  renderVideoData() {
    return (
      <div>
        <VideoData
          video={this.props.video || {}}
          updateVideo={this.updateVideo}
          editable={this.props.videoEditOpen}
          formName={formNames.videoData}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
          validateKeywords={this.validateKeywords}
          composerKeywordsToYouTube={this.composerKeywordsToYouTube}
          canonicalVideoPageExists={canonicalVideoPageExists(this.props.usages)}
        />
      </div>
    );
  }

  renderYoutubeData() {
    return (
      <YoutubeData
        video={this.props.video || {}}
        updateVideo={this.updateVideo}
        editable={this.props.videoEditOpen}
        formName={formNames.youtubeData}
        updateErrors={this.props.formErrorActions.updateFormErrors}
        updateWarnings={this.props.formErrorActions.updateFormWarnings}
        validateYouTubeKeywords={this.validateYouTubeKeywords}
      />
    );
  }

  renderManagement() {
    return (
      <div>
        <ContentChangeDetails
          video={this.props.video || {}}
          updateVideo={this.updateVideo}
        />
        <DurationReset
          video={this.props.video || {}}
          editable={this.props.videoEditOpen}
        />
        <Flags
          video={this.props.video || {}}
          updateVideo={this.updateVideo}
          editable={this.props.videoEditOpen}
          formName={formNames.flags}
          updateErrors={this.props.formErrorActions.updateFormErrors}
          updateWarnings={this.props.formErrorActions.updateFormWarnings}
        />
        {this.renderWorkflow(this.props)}
      </div>
    );
  }

  renderTabs() {
    return (
      <div className="video__detailbox">
        <Tabs>
          <TabList>
            <Tab>Video Data</Tab>
            <Tab>YouTube Data</Tab>
            <Tab>Management</Tab>
            <Tab>Usages</Tab>
            <Tab>Targeting</Tab>
          </TabList>
          <TabPanel>
            {this.renderVideoDataEditButton()}
            {this.renderVideoData()}
          </TabPanel>
          <TabPanel>
            {this.renderYoutubeData()}
          </TabPanel>
          <TabPanel>
            {this.renderManagement()}
          </TabPanel>
          <TabPanel>
            {this.renderUsages(this.props)}
          </TabPanel>
          <TabPanel>
            {this.renderTargeting(this.props)}
          </TabPanel>
        </Tabs>
      </div>
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
import * as videoPageUpdate from '../../actions/VideoActions/videoPageUpdate';

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
