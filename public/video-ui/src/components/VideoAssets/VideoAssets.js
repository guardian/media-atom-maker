import React from 'react';
import VideoAssetItem from './VideoAssetItem';
import VideoAssetAdd from '../VideoAssetAdd/VideoAssetAdd';

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

  createAsset = (asset) => {
    this.props.videoActions.createAsset(asset, this.props.video.id);
  };

  revertAsset = (videoId, version) => {
    this.props.videoActions.revertAsset(this.props.video.id, videoId, version);
    this.hideAssetList();
  };

  updateVideo = (video) => {
    this.props.videoActions.updateVideo(video);
  };

  renderList() {
      if(this.props.video.assets) {
        return (
          <ul className="asset-list">
            {this.renderCurrentItem()}
            {this.renderListItems()}
          </ul>
        );
      } else {
        return (<p>No assets found</p>);
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
      );
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
      );
    }
  };

  renderAssetEdit() {
    if (this.state.showAssetForm) {
      return (
        <form className="form baseline-margin">
          <VideoAssetAdd createAsset={this.createAsset} hideAssetForm={this.hideAssetForm} {...this.props} />
        </form>
      );
    }

    return false;
  }


  render() {
    return (
        <div className="video-assets">
          <div className="section-header">
            <span className="video__detailbox__header">Assets</span>
            {!this.state.showAssetForm ? <button type="button" onClick={this.showAssetForm}><i className="icon icon__edit">add</i></button> : false}
          </div>
          {this.renderAssetEdit()}
          {this.renderList()}
          {!this.props.video.assets.length ? <span>No assets found</span> : false}
          {!this.state.showAssetList && this.props.video.assets.length ? <button className="video-assets__show-btn" type="button" onClick={this.showAssetList}>Show all assets</button> : false}
        </div>
    );
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
