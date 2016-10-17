import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import VideoSave from '../VideoEdit/VideoSave';

class VideoCreate extends React.Component {

  constructor(props) {
    super(props);
  }

  createVideo = () => {
    this.props.videoActions.createVideo(this.props.video);
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  render () {

    return (
      <div>
        <VideoEdit videoEditable='true' video={this.props.video || {}} updateVideo={this.updateVideo} />
        <VideoSave video={this.props.video} />
      </div>
    );

  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createVideo from '../../actions/VideoActions/createVideo';
import * as updateVideo from '../../actions/VideoActions/updateVideo';

function mapStateToProps(state) {
  return {
    video: state.video
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, updateVideo, createVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoCreate);
