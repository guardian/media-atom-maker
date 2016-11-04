import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import VideoAssets from '../VideoAssets/VideoAssets';
import VideoDetails from '../VideoDetails/VideoDetails';
import VideoPreview from '../VideoPreview/VideoPreview';
import SaveButton from '../utils/SaveButton';

class VideoDisplay extends React.Component {

  state = {
    editable: false
  };

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  saveVideo = () => {
    this.props.videoActions.saveVideo(this.props.video);
    this.disableEditing();
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  enableEditing = () => {
    this.setState({
      editable: true
    });
  };

  disableEditing = () => {
    this.setState({
      editable: false
    });
  };

  renderDetails = () => {
    if(this.state.editable) {
      return (
        <div className="video__sidebar video-details">
          <form className="form video__sidebar__group">
            <VideoEdit video={this.props.video || {}} updateVideo={this.updateVideo}/>
            <SaveButton saveState={this.props.saveState} onSaveClick={this.saveVideo}/>
          </form>
        </div>
      )
    } else {
      return <VideoDetails video={this.props.video || {}} enableEditing={this.enableEditing} />
    }
  }

  render() {
    const video = this.props.video && this.props.params.id === this.props.video.id ? this.props.video : undefined;

    if (!video) {
      return <div className="container">Loading... </div>;
    }

    return (
        <div className="video">
          <VideoAssets video={this.props.video || {}} />

          <VideoPreview video={this.props.video || {}} />

          {this.renderDetails()}
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

function mapStateToProps(state) {
  return {
    video: state.video,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, saveVideo, updateVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);

