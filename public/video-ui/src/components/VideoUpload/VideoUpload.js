import React from 'react';
import VideoTrail from './VideoTrail';
import { getStore } from '../../util/storeAccessor';
import AddAssetFromURL from './AddAssetFromURL';
import PlutoProjectPicker from '../Pluto/PlutoProjectPicker';
import AddSelfHostedAsset from './AddSelfHostedAsset';
import AddYouTubeAsset from './AddYouTubeAsset';

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

    return (
      <div>
        <div className="video__main">
          <div className="video__main__header">
            <div className="video__detailbox">
              <PlutoProjectPicker
                video={this.props.video || {}}
                projects={this.props.pluto.projects}
                saveVideo={this.props.videoActions.saveVideo}
              />
              <AddYouTubeAsset
                video={this.props.video || {}}
                categories={this.props.youtube.categories}
                channels={this.props.youtube.channels}
                uploading={uploading}
                saveVideo={this.props.videoActions.saveVideo}
                startUpload={this.props.uploadActions.startUpload}
              />
              <AddAssetFromURL
                video={this.props.video}
                createAsset={this.props.videoActions.createAsset}
              />
              <AddSelfHostedAsset
                video={this.props.video || {}}
                permissions={getStore().getState().config.permissions}
                uploading={uploading}
                startUpload={this.props.uploadActions.startUpload}
              />
            </div>
            <VideoTrail
              video={this.props.video}
              activeVersion={activeVersion}
              s3Upload={this.props.s3Upload}
              uploads={this.props.uploads}
              selectAsset={version =>
                this.props.videoActions.revertAsset(
                  this.props.video.id,
                  version
                )}
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
