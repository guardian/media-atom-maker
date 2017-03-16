import React from 'react';
import VideoPlutoAdd from './VideoPlutoAdd';

class VideoPlutoList extends React.Component{

  componentWillMount() {
    this.props.plutoVideoActions.getPlutoVideos();
  }

  renderPlutoVideo(video) {
    const videoLink = '/videos/' + video.id;

    return (
      <tr key={video.id}>
        <td>{video.title}</td>
        <td><a href={videoLink}>video page</a></td>
        <td>
          <VideoPlutoAdd
            video={video}
            onProjectAdd={this.props.plutoVideoActions.addProject} />
        </td>
      </tr>
    );
  }

  renderPlutoVideos() {

    if (this.props.plutoVideos.length > 0) {
      return (
        <table className="table">
          <thead className="table__header">
            <tr className="table__header-row">
              <th>Title</th>
              <th>View Video Atom</th>
              <th>Pluto Project</th>
            </tr>
          </thead>
          <tbody>
            {this.props.plutoVideos.map(video => this.renderPlutoVideo(video))}
          </tbody>
        </table>
      );
    }

    return (
      <div className="empty-pluto-container">There are no videos without pluto ids in need of processing</div>
    );
  }


  render() {
    if (this.props.plutoVideos) {
      return (
        <div>
          <div className="video__detail__page">
            <span className="video__detailbox__header">Videos without pluto projects</span>
          </div>
          <div>
              {this.renderPlutoVideos()}
          </div>
        </div>
      );
    }

    return (<div>Loading...</div>);
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getPlutoVideos from '../../actions/PlutoIdActions/getPlutoVideos';
import * as addProject from '../../actions/PlutoIdActions/addProject';

function mapStateToProps(state) {
  return {
    plutoVideos: state.plutoVideos
  };
}

function mapDispatchToProps(dispatch) {
  return {
    plutoVideoActions: bindActionCreators(Object.assign({}, getPlutoVideos, addProject), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoPlutoList);
