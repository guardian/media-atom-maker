import React from 'react';

import Header from './Header';

class ReactApp extends React.Component {
  constructor(props) {
    super(props);
  }

  state = {
    fetchedVideoFor: ""
  };

  componentWillMount() {
    if (this.props.params.id) {
      this.props.appActions.getVideo(this.props.params.id);
    }
  }

  componentWillReceiveProps() {
    if (this.props.params.id &&
        (!this.props.video || this.props.params.id !== this.props.video.id) &&
        this.state.fetchedVideoFor !== this.props.params.id
      ) {
      this.props.appActions.getVideo(this.props.params.id);
      this.setState({
        fetchedVideoFor: this.props.params.id
      });
    }
  }

  updateSearchTerm = (searchTerm) => {
    this.props.appActions.updateSearchTerm(searchTerm);
  };

  render() {
    return (
        <div className="wrap">
          <Header
            updateSearchTerm={this.updateSearchTerm}
            searchTerm={this.props.searchTerm}
            searching={this.props.saveState.searching}
            video={this.props.video || {}}
            showPublishedState={this.props.params.id ? true : false}
            publishVideo={this.props.appActions.publishVideo}
            saveState={this.props.saveState}
          />
          {this.props.error ? <div className="error-bar">{this.props.error}</div> : false}
          <div>
            {this.props.children}
          </div>
        </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as updateSearchTerm from '../actions/SearchActions/updateSearchTerm';
import * as getVideo from '../actions/VideoActions/getVideo';
import * as publishVideo from '../actions/VideoActions/publishVideo';
import * as saveVideo from '../actions/VideoActions/saveVideo';

function mapStateToProps(state) {
  return {
    searchTerm: state.searchTerm,
    saveState: state.saveState,
    video: state.video,
    error: state.error
  };
}

function mapDispatchToProps(dispatch) {
  return {
    appActions: bindActionCreators(Object.assign({}, updateSearchTerm, getVideo, publishVideo, saveVideo), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(ReactApp);
