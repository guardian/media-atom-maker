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

  updateAsset = (asset) => {
    this.props.videoActions.updateAsset(asset);
  };

  renderList() {
      if(this.props.video.data.assets) {
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
        this.props.video.data.assets.map((asset, index) => <VideoAssetItem key={index} asset={asset} activeAsset={this.props.video.data.activeVersion}/>)
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
        <div className="video__sidebar video-assets">
          {this.renderAssetEdit()}
          {this.renderList()}

        </div>
    )
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as updateAsset from '../../actions/VideoActions/updateAsset';

function mapStateToProps(state) {
  return {
    asset: state.asset
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, createAsset, updateAsset), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoAssets);