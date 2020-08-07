import React from 'react';
import PropTypes from 'prop-types';
import VideoItem from '../../components/VideoItem';
import { frontPageSize } from '../../constants/frontPageSize';

class Videos extends React.Component {
  static propTypes = {
    videos: PropTypes.array.isRequired
  };

  componentDidMount() {
    this.props.videoActions.getVideos(this.props.searchTerm, this.props.limit);
  }

  componentDidUpdate(prevProps) {
    const oldSearch = this.props.searchTerm;
    const newSearch = prevProps.searchTerm;
    if (newSearch !== oldSearch) {
      this.props.videoActions.getVideos(newSearch, this.props.limit);
    }
  }

  renderList() {
    if (this.props.videos.length) {
      return (
        <ul className="grid__list">
          {this.renderListItems()}
        </ul>
      );
    } else {
      return <p className="grid__message">No videos found</p>;
    }
  }

  renderListItems() {
    return this.props.videos.map(video => (
      <VideoItem key={video.id} video={video} />
    ));
  }

  renderMoreLink() {
    if (this.props.videos.length === this.props.total) {
      return false;
    }

    const showMore = () => {
      this.props.videoActions.getVideos(
        this.props.searchTerm,
        this.props.limit + frontPageSize
      );
    };

    return (
      <div>
        <button className="btn video__load_more" onClick={showMore}>
          Load More
        </button>
      </div>
    );
  }

  render() {
    return (
      <div>
        <div className="grid">
          {this.renderList()}
        </div>
        {this.renderMoreLink()}
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideos from '../../actions/VideoActions/getVideos';

function mapStateToProps(state) {
  return {
    videos: state.videos.entries,
    total: state.videos.total,
    limit: state.videos.limit,
    searchTerm: state.searchTerm
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, getVideos), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(React.memo(Videos));
