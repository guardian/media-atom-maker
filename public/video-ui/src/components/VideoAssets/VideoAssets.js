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
    showAssetForm: false,
    showAssetList: false
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

  showAssetList = () => {
    this.setState({
      showAssetList: true
    });
  };

  hideAssetList = () => {
    this.setState({
      showAssetList: false
    });
  };

  createAsset = () => {
    this.props.videoActions.createAsset(this.props.asset, this.props.video.id);
  };

  revertAsset = (version) => {
    this.props.videoActions.revertAsset(version, this.props.video.id);
    this.hideAssetList();
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  updateAsset = (asset) => {
    this.props.videoActions.updateAsset(asset);
  };

  renderList() {
      if(this.props.video.assets) {
        return (
          <ul className="asset-list">
            {this.renderCurrentItem()}
            {this.renderListItems()}
          </ul>
        )
      } else {
        return (<p>No assets found</p>)
      }
  }

  isCurrentAsset = (asset) => {
    return asset.version === this.props.video.activeVersion;
  };

  renderCurrentItem() {
    return this.props.video.assets.filter(this.isCurrentAsset).map((asset, index) => <VideoAssetItem key={index} asset={asset}
                                                                                               activeAsset={this.isCurrentAsset(asset)}
                                                                                               video={this.props.video}
                                                                                               revertAsset={this.revertAsset}
                                                                                               updateVideo={this.updateVideo}/>);
  }

  renderListItems() {
    if (this.state.showAssetList) {
      return (
        this.props.video.assets.map(this.mapListItems)
      )
    }

    return false;
  }

  mapListItems = (asset, index) => {
    if (!this.isCurrentAsset(asset)) {
      return (
        <VideoAssetItem key={index} asset={asset}
                        activeAsset={this.isCurrentAsset(asset)}
                        video={this.props.video}
                        revertAsset={this.revertAsset}
                        updateVideo={this.updateVideo}/>
      )
    }
  };

  renderAssetEdit() {
    if (this.state.showAssetForm) {
      return (
        <form className="form baseline-margin--bottom">
          <VideoAssetAdd updateAsset={this.updateAsset} {...this.props} />
          <div className="btn__group">
            <button className="btn" type="button" onClick={this.createAsset}>Save</button>
            <button className="btn" type="button" onClick={this.hideAssetForm}>Cancel</button>
          </div>
        </form>
      )
    }

    return false;
  }


  render() {
    return (
        <div className="video-assets">
          <div className="section-header">
            {!this.state.showAssetForm ? <button className="btn section-header__btn" type="button" onClick={this.showAssetForm}>Add new asset</button> : false}
          </div>
          {this.renderAssetEdit()}
          {this.renderList()}
          {!this.props.video.assets.length ? <span>No assets found</span> : false}
          {!this.state.showAssetList && this.props.video.assets.length ? <button className="video-assets__show-btn" type="button" onClick={this.showAssetList}>Show all assets</button> : false}
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
import * as updateAsset from '../../actions/VideoActions/updateAsset';

function mapStateToProps(state) {
  return {
    asset: state.asset,
    video: state.video
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, createAsset, updateVideo, revertAsset, updateAsset), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoAssets);
