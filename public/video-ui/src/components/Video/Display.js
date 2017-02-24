import React from 'react';
import VideoAssets from '../VideoAssets/VideoAssets';
import VideoSelectBar from '../VideoSelectBar/VideoSelectBar';
import VideoPreview from '../VideoPreview/VideoPreview';
import VideoUsages from '../VideoUsages/VideoUsages';
import VideoMetaData from '../VideoMetaData/VideoMetaData';
import YoutubeMetaData from '../YoutubeMetaData/YoutubeMetaData';
import VideoPoster from '../VideoPoster/VideoPoster';
import GridImageSelect from '../utils/GridImageSelect';
import {getVideoBlock} from '../../util/getVideoBlock';
import {getStore} from '../../util/storeAccessor';
import Icon from '../Icon';

class VideoDisplay extends React.Component {

  state = {
    metadataEditable: false,
    youtubeEditable: false
  };

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
    this.props.videoActions.getUsages(this.props.params.id);
  }

  saveVideo = () => {
    this.props.videoActions.saveVideo(this.props.video);

    this.setState({
      editable: false
    });
  };

  saveAndUpdateVideo = (video) => {
    this.props.videoActions.debouncedSaveVideo(video);
  };

  selectVideo = () => {
    window.parent.postMessage({atomId: this.props.video.id}, '*');
  };

  manageEditingState = (value, property) => {

    if (property === 'metadata') {

      this.setState({
        metadataEditable: value
      });

    } else if (property === 'youtube') {

      this.setState({
        youtubeEditable: value
      });
    }
  };

  disableEditing = () => {
    this.setState({
      editable: false
    });
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

  renderCreateButton = () => {
    const filterUsageType = this.props.usages.filter(value => value.type === 'video');

    if(filterUsageType.length === 0){
      return (
        <button className="button__secondary" onClick={this.pageCreate}><Icon icon="add_to_queue"></Icon> Create Video Page</button>
      )
    }
  }


  renderEditButton = (editable, property) => {

    if (editable) {
      return (
        <Icon className="icon__done" icon="done" onClick={this.manageEditingState.bind(this, false, property)}/>
      );
    } else {
      return (
        <Icon className="icon__edit" icon="edit" onClick={this.manageEditingState.bind(this, true, property)}/>
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
                  {this.renderEditButton(this.state.metadataEditable, 'metadata')}
                </div>
                <VideoMetaData
                  component={VideoMetaData}
                  video={this.props.video || {}}
                  saveAndUpdateVideo={this.saveAndUpdateVideo}
                  editable={this.state.metadataEditable}
                 />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Youtube Meta Data</header>
                  {this.renderEditButton(this.state.youtubeEditable, 'youtube')}
                </div>
                <YoutubeMetaData
                  component={YoutubeMetaData}
                  video={this.props.video || {}}
                  saveVideo={this.saveVideo}
                  saveAndUpdateVideo={this.saveAndUpdateVideo}
                  disableStatusEditing={this.cannotEditStatus()}
                  editable={this.state.youtubeEditable}
                />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Poster Image</header>
                  <GridImageSelect video={this.props.video || {}} updateVideo={this.saveAndUpdateVideo} gridUrl={this.props.config.gridUrl} createMode={false}/>
                </div>
                <VideoPoster
                  video={this.props.video || {}}
                  updateVideo={this.saveAndUpdateVideo}
                  editable={this.state.editable}
                />
              </div>
              <div className="video__detailbox">
                <div className="video__detailbox__header__container">
                  <header className="video__detailbox__header">Usages</header>
                  {this.renderCreateButton()}
                </div>
                <VideoUsages
                  video={this.props.video || {}}
                  publishedVideo={this.props.publishedVideo || {}}
                  usages={this.props.usages || []}
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
import * as videoUsages from '../../actions/VideoActions/videoUsages';
import * as videoPageCreate from '../../actions/VideoActions/videoPageCreate';
import * as getPublishedVideo from '../../actions/VideoActions/getPublishedVideo';

function mapStateToProps(state) {
  return {
    video: state.video,
    config: state.config,
    usages: state.usage,
    composerPageWithUsage: state.pageCreate,
    publishedVideo: state.publishedVideo
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, saveVideo, videoUsages, videoPageCreate, getPublishedVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
