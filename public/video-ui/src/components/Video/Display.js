import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import VideoAssets from '../VideoAssets/VideoAssets';
import VideoPublishBar from '../VideoPublishBar/VideoPublishBar';
import VideoSelectBar from '../VideoSelectBar/VideoSelectBar';
import VideoPreview from '../VideoPreview/VideoPreview';
import VideoUsages from '../VideoUsages/VideoUsages';

class VideoDisplay extends React.Component {

  state = {
    editable: false
  };

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  saveVideo = () => {
    this.props.videoActions.saveVideo(this.props.video);
    this.setState({
      editable: false
    })
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  resetVideo = () => {
    this.props.videoActions.getVideo(this.props.video.id);
  };

  publishVideo = () => {
    this.props.videoActions.publishVideo(this.props.video.id);
    this.setState({
      editable: false
    });
  };

  pageCreate = () => {
    const videoPage = {
      elements: [
        {
          elementType: 'content-atom',
          fields: {
            id: this.props.video.id,
            atomType: 'media',
            required: 'true',
            title: this.props.video.title,
            published: 'Unable to get published state from atom',
            isMandatory: 'true',
            editorialLink: ''

          },
          assets: []
        }
      ]
    };

    this.props.videoActions.createPage(this.props.video.id, this.props.video.title, videoPage)
  };

  selectVideo = () => {
    window.parent.postMessage({atomId: this.props.video.id}, '*');
  };

  enableEditing = () => {
    this.setState({
      editable: true
    });
  };

  render() {
    const video = this.props.video && this.props.params.id === this.props.video.id ? this.props.video : undefined;

    if (!video) {
      return <div className="container">Loading... </div>;
    }

    return (
      <div>
        <VideoPublishBar video={this.props.video} saveState={this.props.saveState} publishVideo={this.publishVideo} />
        <VideoSelectBar onSelectVideo={this.selectVideo} embeddedMode={this.props.config.embeddedMode} />

        <div className="video">
          <div className="video__sidebar video-details">
            <form className="form video__sidebar__group">

              <VideoEdit
                video={this.props.video || {}}
                updateVideo={this.updateVideo}
                saveVideo={this.saveVideo}
                resetVideo={this.resetVideo}
                saveState={this.props.saveState}
               />

              <VideoPublishButton video={this.props.video || {}} publishVideo={this.publishVideo} />
            </form>
          </div>

          <div className="video__main">
            <div className="video__main__header">
              <div className="video__detailbox">
                <span className="video__detailbox__header">Preview</span>
                <VideoPreview video={this.props.video || {}} />
              </div>
              <div className="video__detailbox usages">
                <span className="video__detailbox__header">Usages</span>
                <VideoUsages video={this.props.video} />
              </div>
            </div>
            <div className="video__detailbox">
              <VideoAssets video={this.props.video || {}} />
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
import * as publishVideo from '../../actions/VideoActions/publishVideo';
import * as createPage from '../../actions/VideoActions/createPage';

function mapStateToProps(state) {
  return {
    video: state.video,
    saveState: state.saveState,
    config: state.config
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, saveVideo, updateVideo, publishVideo, createPage), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);
