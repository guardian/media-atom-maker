import React from 'react';

class VideoPlutoList extends React.Component{

  componentWillMount() {
    this.props.plutoVideoActions.getPlutoVideos();
  }

  render() {
    return (<div>Hello</div>);
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
