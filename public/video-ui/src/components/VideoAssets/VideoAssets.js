import React, {PropTypes} from 'react';
import {Link} from 'react-router';
import VideoAssetItem from './VideoAssetItem';
import VideoAssetAdd from '../VideoAssetAdd/VideoAssetAdd';
import SaveButton from '../utils/SaveButton';

class VideoAssets extends React.Component {

  componentDidMount() {
    this.props.videoActions.populateEmptyAsset();
  }

  state = {
    showAssetForm: false
  };

  showAssetForm = () => {
    this.setState({
      showAssetForm: true
    });
  };

  hideAssetForm = () => {
    this.setState({
      showAssetForm: false
    });
  };

  createAsset = () => {
    this.props.videoActions.createAsset(this.props.asset, this.props.video.id);
  };

  revertAsset = (version) => {
    this.props.videoActions.revertAsset(version, this.props.video.id);
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  renderList() {
      if(this.props.video.assets) {
        return (
          <ul className="asset-list">
            {this.renderListItems()}
          </ul>
        )
      } else {
        return (<p>No assets found</p>)
      }
  }

  renderListItems() {
    return (
        this.props.video.assets.map((asset, index) => <VideoAssetItem key={index} asset={asset} activeAsset={this.props.video.activeVersion} video={this.props.video} revertAsset={this.revertAsset}  updateVideo={this.updateVideo}/>)
    );
  }

  renderAssetEdit() {
    if (this.state.showAssetForm) {
      return (
        <form className="form">
          <VideoAssetAdd updateAsset={this.updateAsset} {...this.props} />
          <SaveButton onSaveClick={this.createAsset}/>
          <button className="btn" type="button" onClick={this.hideAssetForm}>Cancel</button>
        </form>
      )
    } else {
      return (
        <button className="btn" type="button" onClick={this.showAssetForm}>Add new asset</button>
      )
    }
  }


  render() {
    return (
        <div className="video-assets">
          <h2>All Assets</h2>
          {this.renderList()}
          {this.renderAssetEdit()}
        </div>
    )
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as updateVideo from '../../actions/VideoActions/updateVideo';
import * as revertAsset from '../../actions/VideoActions/revertAsset';

function mapStateToProps(state) {
  return {
    asset: state.asset,
    video: state.video
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, createAsset, updateVideo, revertAsset), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoAssets);
