import React, {PropTypes} from 'react';
import {Link} from 'react-router';
import VideoItem from './VideoItem';

class Videos extends React.Component {

  static propTypes = {
    videos: PropTypes.array.isRequired,
  }

  componentDidMount() {
    this.props.videoActions.getVideos();
  }

  renderList() {
    if(this.props.videos.length) {
      return (
          <ul className="grid__list">
            {this.renderListItems()}
          </ul>)
    } else {
        return (<p className="grid__message">No videos found</p>)
    }
  }

  renderListItems() {
    return (
        this.props.videos.map((video) => <VideoItem key={video.id} video={video} />)
    );
  }


  render() {
    return (
        <div>
          <div className="grid">
            {this.renderList()}
          </div>
        </div>
    )
  }
}


//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideos from '../../actions/VideoActions/getVideos';

function mapStateToProps(state) {
  return {
    videos: state.videos
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideos), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Videos);
