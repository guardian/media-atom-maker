import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import VideoAssets from '../VideoAssets/VideoAssets';
import VideoSelectBar from '../VideoSelectBar/VideoSelectBar';
import VideoPreview from '../VideoPreview/VideoPreview';
import VideoAuditTrail from '../VideoAuditTrail/VideoAuditTrail';
import VideoUsages from '../VideoUsages/VideoUsages';
import VideoMetaData from '../VideoMetaData/VideoMetaData';
import YoutubeMetaData from '../YoutubeMetaData/YoutubeMetaData';

class VideoDisplay extends React.Component {

  componentWillReceiveProps(nextProps) {
    if (nextProps.video != this.props.video) {
      this.props.videoActions.getAudits(nextProps.video.id);
    }
  }

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  saveVideo = () => {
    this.props.videoActions.saveVideo(this.props.video);
  };

  saveAndUpdateVideo = (video) => {
    this.props.videoActions.saveVideo(video);
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  resetVideo = () => {
    this.props.videoActions.getVideo(this.props.video.id);
  };

  selectVideo = () => {
    window.parent.postMessage({atomId: this.props.video.id}, '*');
  };

  enableEditing = () => {
    this.setState({
      editable: true
    });
  };

  cannotEditStatus = () => {
    return this.props.video.expiryDate <= Date.now()
  };

  render() {
    const video = this.props.video && this.props.params.id === this.props.video.id ? this.props.video : undefined;

    if (!video) {
      return <div className="container">Loading... </div>;
    }

    return (
      <div>
        <VideoSelectBar video={video} onSelectVideo={this.selectVideo} embeddedMode={this.props.config.embeddedMode} />

        <div className="video">
          <div className="video__main">
            <div className="video__main__header">
              <div className="video__detailbox">
                <span className="video__detailbox__header">Preview</span>
                <VideoPreview video={this.props.video || {}} />
              </div>
              <div className="video__detailbox">
                <span className="video__detailbox__header">Video Meta Data</span>
                <VideoMetaData
                  component={VideoMetaData}
                  video={this.props.video || {}}
                  updateVideo={this.updateVideo}
                  saveVideo={this.saveVideo}
                  saveAndUpdateVideo={this.saveAndUpdateVideo}
                  resetVideo={this.resetVideo}
                  saveState={this.props.saveState}
                  disableStatusEditing={this.cannotEditStatus()}
                 />
              </div>
              <div className="video__detailbox">
                <span className="video__detailbox__header">Youtube Meta Data</span>
                <YoutubeMetaData
                  component={YoutubeMetaData}
                  video={this.props.video || {}}
                  updateVideo={this.updateVideo}
                  saveVideo={this.saveVideo}
                  saveAndUpdateVideo={this.saveAndUpdateVideo}
                  resetVideo={this.resetVideo}
                  saveState={this.props.saveState}
                  disableStatusEditing={this.cannotEditStatus()}
                 />
              </div>
              <div className="video__detailbox usages">
                <span className="video__detailbox__header">Usages</span>
                <VideoUsages
                  video={this.props.video || {}}
                  fetchUsages={this.props.videoActions.getUsages}
                  usages={this.props.usages[this.props.video.id] || {}}
                  composerPageWithUsage={this.props.composerPageWithUsage[this.props.video.id] || {}}
                  createComposerPage={this.props.videoActions.createVideoPage}
                />
              </div>
            </div>
            <div className="video__detailbox">
              <VideoAssets video={this.props.video || {}} />
            </div>

            <div className="video__detailbox">
              <span className="video__detailbox__header">Atom Audit Trail</span>
              <VideoAuditTrail video={this.props.video || {}} audits={this.props.audits || []}/>
            </div>
          </div>
        </div>
      </div>
    )
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
import * as getAudits from '../../actions/VideoActions/getAudits';

function mapStateToProps(state) {
  return {
    video: state.video,
    saveState: state.saveState,
    config: state.config,
    usages: state.usage,
    composerPageWithUsage: state.pageCreate,
    audits: state.audits
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, saveVideo, updateVideo, videoUsages, videoPageCreate, getAudits), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
