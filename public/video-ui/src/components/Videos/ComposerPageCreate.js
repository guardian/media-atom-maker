import React from 'react';
import {getVideoBlock} from '../../util/getVideoBlock';
import Icon from '../Icon';
import {getStore} from '../../util/storeAccessor';

class ComposerPageCreate extends React.Component {

  componentWillMount() {
    if (this.props.video) {
      this.props.videoActions.getUsages(this.props.video.id);
    }
  }

  state = {
    composerUpdateInProgress: false
  };

  getVideoMetadata = () => {
    return {
      headline: this.props.video.title,
      standfirst: this.props.video.description ? '<p>' + this.props.video.description + '</p>' : null
    };
  }

  getComposerUrl = () => {
    return getStore().getState().config.composerUrl;
  }

  composerPageExists = () => {
    return  this.props.usages.filter(value => value.type === 'video').length > 0;
  }


  pageCreate = () => {
    this.setState({
      composerUpdateInProgress: true
    });

    const metadata = this.getVideoMetadata();

    const videoBlock = getVideoBlock(this.props.video.id, metadata);

    return this.props.videoActions.createVideoPage(this.props.video.id, metadata, this.getComposerUrl(), videoBlock)
    .then(() => {
      this.setState({
        composerUpdateInProgress: false
      });
    });

  };

  render() {

    if (this.composerPageExists()) {
      return null;
    }

    return (
      <button
        className="button__secondary"
        onClick={this.pageCreate}
        disabled={this.props.videoEditOpen || this.state.composerUpdateInProgress }
      >
          <Icon icon="add_to_queue"></Icon> Create Video Page
      </button>
    );
  };
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as videoUsages from '../../actions/VideoActions/videoUsages';
import * as videoPageCreate from '../../actions/VideoActions/videoPageCreate';
import * as updateVideoEditState from '../../actions/VideoActions/updateVideoEditState';

function mapStateToProps(state) {
  return {
    usages: state.usage,
    videoEditOpen: state.videoEditOpen,
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({},  videoUsages, videoPageCreate, updateVideoEditState), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(ComposerPageCreate);
