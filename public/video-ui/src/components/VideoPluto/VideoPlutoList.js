import React from 'react';
import VideoPlutoAdd from './VideoPlutoAdd';

class VideoPlutoList extends React.Component{

  componentWillMount() {
    this.props.plutoVideoActions.getPlutoVideos();
  }

  selectPlutoId(video) {
    this.props.plutoVideoActions.addProject(video.id, video.plutoProjectId);
  }

  renderPlutoVideo(video) {
    return (
      <tr key={video.id}>
        <td>{video.title}</td>
        <td>{video.description}</td>
        <td>
          <VideoPlutoAdd
            video={video}
            onProjectAdd={this.props.plutoVideoActions.addProject} />
        </td>
      </tr>
    );
  }

  renderPlutoVideos() {
    return (
      <tbody>
        {this.props.plutoVideos.map(video => this.renderPlutoVideo(video))}
      </tbody>
    );
  }


  render() {
    if (this.props.plutoVideos) {
      return (
        <div>
          <div className="video__detailbox">
            <span className="video__detailbox__header">Videos without pluto projects</span>
          </div>
          <div>
            <table className="table">
              <thead className="table__header">
                <tr className="table__header-row">
                  <th>Title</th>
                  <th>Description</th>
                  <th>Pluto Project</th>
                </tr>
              </thead>
              {this.renderPlutoVideos()}
            </table>
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
