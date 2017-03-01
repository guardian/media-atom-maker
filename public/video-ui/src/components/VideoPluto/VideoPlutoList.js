import React from 'react';

class VideoPlutoList extends React.Component{

  componentWillMount() {
    this.props.plutoVideoActions.getPlutoVideos();
  }

  renderPlutoVideo(video) {
    return (
      <tr key={video.id}>
        <td>{video.title}</td>
        <td>{video.description}</td>
      </tr>
    );
  }

  renderPlutoVideos() {
    return (
      <tbody>
        {this.props.plutoVideos.map(this.renderPlutoVideo)}
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

function mapStateToProps(state) {
  return {
    plutoVideos: state.plutoVideos
  };
}

function mapDispatchToProps(dispatch) {
  return {
    plutoVideoActions: bindActionCreators(Object.assign({}, getPlutoVideos), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoPlutoList);
