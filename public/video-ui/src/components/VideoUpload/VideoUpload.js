import React from 'react';
import VideoTrail from './VideoTrail';
import { getStore } from '../../util/storeAccessor';
import YoutubeMetaData from '../YoutubeMetaData/YoutubeMetaData';
import AddAssetFromURL from './AddAssetFromURL';
import UploadPicker from './UploadPicker';
import { channelAllowed } from '../../util/channelAllowed';

class VideoUpload extends React.Component {
  hasCategories = () =>
    this.props &&
    this.props.youtube &&
    this.props.youtube.categories.length !== 0;
  hasChannels = () =>
    this.props.youtube && this.props.youtube.channels.length !== 0;
  hasPlutoProjects = () =>
    this.props.pluto && this.props.pluto.projects.length !== 0;

  componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
    if (!this.hasPlutoProjects()) {
      this.props.plutoActions.getProjects();
    }
    if (!this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
  }

  render() {
    const uploading = this.props.s3Upload.total > 0;
    const activeVersion = this.props.video ? this.props.video.activeVersion : 0;

    const { channels } = this.props.youtube;
    const permissions = getStore().getState().config.permissions;

    const canSelfHost = permissions.addSelfHostedAsset;
    const canUploadToYouTube = channelAllowed(this.props.video, channels);

    const selectAsset = version => {
      this.props.videoActions.revertAsset(this.props.video.id, version);
    };

    return (
      <div>
        <div className="video__main">
          <div className="video__main__header">
            <div className="video__detailbox">
              <YoutubeMetaData
                video={this.props.video || {}}
                saveVideo={this.props.videoActions.saveVideo}
                youtube={this.props.youtube}
                pluto={this.props.pluto}
                assets={this.props.video.assets}
                editable={!uploading}
              />
              <div className="upload__actions upload__actions--non-empty">
                <UploadPicker
                  canSelfHost={canSelfHost}
                  canUploadToYouTube={canUploadToYouTube}
                  video={this.props.video}
                  uploading={uploading}
                  uploadActions={this.props.uploadActions}
                />
                <AddAssetFromURL
                  video={this.props.video}
                  createAsset={this.props.videoActions.createAsset}
                />
              </div>
            </div>
            <VideoTrail
              activeVersion={activeVersion}
              s3Upload={this.props.s3Upload}
              uploads={this.props.uploads}
              selectAsset={selectAsset}
              getUploads={() =>
                this.props.uploadActions.getUploads(this.props.video.id)}
            />
          </div>
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as saveVideo from '../../actions/VideoActions/saveVideo';
import * as getUpload from '../../actions/UploadActions/getUploads';
import * as s3UploadActions from '../../actions/UploadActions/s3Upload';
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as revertAsset from '../../actions/VideoActions/revertAsset';
import * as getProjects from '../../actions/PlutoActions/getProjects';
import * as getCategories from '../../actions/YoutubeActions/getCategories';
import * as getChannels from '../../actions/YoutubeActions/getChannels';

function mapStateToProps(state) {
  return {
    video: state.video,
    s3Upload: state.s3Upload,
    uploads: state.uploads,
    pluto: state.pluto,
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(
      Object.assign({}, getVideo, saveVideo, createAsset, revertAsset),
      dispatch
    ),
    uploadActions: bindActionCreators(
      Object.assign({}, s3UploadActions, getUpload),
      dispatch
    ),
    youtubeActions: bindActionCreators(
      Object.assign({}, getCategories, getChannels),
      dispatch
    ),
    plutoActions: bindActionCreators(Object.assign({}, getProjects), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);
