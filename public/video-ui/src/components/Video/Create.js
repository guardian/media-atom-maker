import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import SaveButton from '../utils/SaveButton';
import {blankVideoData} from '../../constants/blankVideoData';

class VideoCreate extends React.Component {

  constructor(props) {
    super(props);
  }

  componentDidMount() {
    this.props.videoActions.populateEmptyVideo();
  }

  createVideo = () => {
    this.props.videoActions.createVideo(this.props.video);
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  render () {
    return (
      <div className="container">
        <form className="form">
          <VideoEdit video={this.props.video || blankVideoData} updateVideo={this.updateVideo} />
          <SaveButton onSaveClick={this.createVideo} />
        </form>
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
