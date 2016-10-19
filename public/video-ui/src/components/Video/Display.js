import React from 'react';
import VideoEdit from '../VideoEdit/VideoEdit';
import SaveButton from '../utils/SaveButton';

class VideoDisplay extends React.Component {

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  saveVideo = () => {
    this.props.videoActions.saveVideo(this.props.video);
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  render() {
    const video = this.props.video && this.props.params.id === this.props.video.id ? this.props.video : undefined;

    if (!video) {
      return <div className="container">Loading... </div>;
    }

    return (
        <div className="container">
          <form className="form">
            <VideoEdit video={this.props.video || {}} updateVideo={this.updateVideo} />
            {/*<SyncValidationForm />*/}
            <SaveButton onSaveClick={this.saveVideo} />
          </form>
        </div>
    );
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
    video: state.video
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo, saveVideo, updateVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);

