import React, {PropTypes} from 'react';
import {Link} from 'react-router';
import VideoAssetItem from './VideoAssetItem';
import VideoAssetAdd from '../VideoAssetAdd/VideoAssetAdd';

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


  render() {
    return (
        <div className="video__sidebar video-assets">
          {this.renderList()}

          {(this.state.showAssetForm ? <VideoAssetAdd {...this.props} /> : "")}

          {(!this.state.showAssetForm ? <button type="button" onClick={this.showAssetForm}>Add new asset</button> : "")}
        </div>
    )
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as createAsset from '../../actions/VideoActions/createAsset';

function mapStateToProps(state) {
  return {
    asset: state.asset
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(Object.assign({}, createAsset), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoAssets);