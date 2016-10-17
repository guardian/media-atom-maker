import React from 'react';

class VideoDisplay extends React.Component {

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
  }

  render() {
    const video = this.props.video && this.props.params.id === this.props.video.id ? this.props.video : undefined;

    if (!video) {
      return <div>Loading... </div>;
    }

    return (
        <div>
          <h2>{video.data.title}</h2>
          <div>
            <p>Type: {video.type}</p>
            <p>Revision: {video.contentChangeDetails.revision}</p>
            <p>No. of Assets: {video.data.assets.length}</p>
          </div>
        </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';

function mapStateToProps(state) {
  return {
    video: state.video
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoDisplay);

