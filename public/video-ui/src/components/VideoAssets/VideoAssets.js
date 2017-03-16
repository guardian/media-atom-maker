import React from 'react';
import VideoAssetItem from './VideoAssetItem';
import VideoAssetAdd from '../VideoAssetAdd/VideoAssetAdd';
import Icon from '../Icon';

class VideoAssets extends React.Component {

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
    this.props.videoActions.createAsset(asset, this.props.video);
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
    if (this.state.showAssetList || !this.props.video.activeVersion) {
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

  renderHeader() {
    const buttons = <div className="video-assets__buttons">
      <a className="button" onClick={this.showAssetForm}>
        <Icon className="icon__edit icon__spacing" icon="add"/>
      </a>
    </div>;

    return <div className="video__detailbox__header__container">
      <span className="video__detailbox__header">Assets</span>
      {this.state.showAssetForm ? false : buttons }
    </div>;
  }

  shouldShowAssetExpander = () => {
    return !this.state.showAssetList && this.props.video.assets.length && !!this.props.video.activeVersion;
  };

  render() {
    return (
        <div className="video-assets">
          {this.renderHeader()}
          {this.renderAssetEdit()}
          {this.renderList()}
          {!this.props.video.assets.length ? <span className="baseline-margin">No assets found</span> : false}
          {this.shouldShowAssetExpander() ? <button className="video-assets__show-btn" type="button" onClick={this.showAssetList}>Show all assets</button> : false}
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
    video: state.video
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, createAsset, updateVideo, revertAsset), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoAssets);
