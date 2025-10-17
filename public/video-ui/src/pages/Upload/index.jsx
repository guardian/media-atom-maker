import React from 'react';
import PlutoProjectLink from '../../components/Pluto/PlutoProjectLink';
import { PlutoProjectPicker } from '../../components/Pluto/PlutoProjectPicker';
import AddAssetFromURL from '../../components/VideoUpload/AddAssetFromURL';
import AddSelfHostedAsset from '../../components/VideoUpload/AddSelfHostedAsset';
import VideoTrail from '../../components/VideoUpload/VideoTrail';
import YoutubeUpload from '../../components/VideoUpload/YoutubeUpload';
import { getStore } from '../../util/storeAccessor';

class VideoUpload extends React.Component {
  hasCategories = () => this.props.youtube?.categories?.length !== 0;
  hasChannels = () => this.props.youtube?.channels?.length !== 0;

  UNSAFE_componentWillMount() {
    this.props.videoActions.getVideo(this.props.params.id);
    if (!this.hasCategories()) {
      this.props.youtubeActions.fetchCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.fetchChannels();
    }
  }

  render() {
    const uploading = this.props.s3Upload.total > 0;
    const activeVersion = this.props.video?.activeVersion ?? 0;

    const projectId = this.props.video?.plutoData?.projectId;

    return (
      <div>
        <div className="video__main">
          <div className="video__main__header">
            <div className="video__detailbox">
              <div>
                <div className="form__group">
                  {projectId && <PlutoProjectLink projectId={projectId} />}
                  <PlutoProjectPicker
                    video={this.props.video || {}}
                    saveVideo={this.props.videoActions.saveVideo}
                  />
                </div>
              </div>
              <YoutubeUpload
                video={this.props.video || {}}
                categories={this.props.youtube.categories}
                channels={this.props.youtube.channels}
                uploading={uploading}
                saveVideo={this.props.videoActions.saveVideo}
                startUpload={this.props.uploadActions.startVideoUpload}
              />
              <AddAssetFromURL
                video={this.props.video}
                createAsset={this.props.videoActions.createAsset}
              />
              <AddSelfHostedAsset
                video={this.props.video || {}}
                permissions={getStore().getState().config.permissions}
                uploading={uploading}
                startUpload={this.props.uploadActions.startVideoUpload}
              />
            </div>
            <VideoTrail
              video={this.props.video}
              activeVersion={activeVersion}
              s3Upload={this.props.s3Upload}
              uploads={this.props.uploads}
              deleteAssets={this.props.videoActions.deleteAssets}
              selectAsset={version =>
                this.props.videoActions.revertAsset(
                  this.props.video.id,
                  version
                )
              }
              getUploads={() => {
                this.props.uploadActions.getUploads(this.props.video.id);
              }}
              startSubtitleFileUpload={
                this.props.uploadActions.startSubtitleFileUpload
              }
              deleteSubtitle={this.props.uploadActions.deleteSubtitle}
              permissions={getStore().getState().config.permissions}
              s3UploadPostProcessing={
                this.props.uploadActions.s3UploadPostProcessing
              }
              s3UploadReset={this.props.uploadActions.s3UploadReset}
              activatingAssetNumber={this.props.isActivatingAssetNumber}
              getVideo={this.props.videoActions.getVideo}
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
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as allDeleteAssetActions from '../../actions/VideoActions/deleteAsset';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as revertAsset from '../../actions/VideoActions/revertAsset';
import * as saveVideo from '../../actions/VideoActions/saveVideo';
import {
  deleteSubtitle,
  resetS3UploadState,
  setS3UploadStatusToPostProcessing,
  startSubtitleFileUpload,
  startVideoUpload
} from '../../slices/s3Upload';
import { getUploads } from '../../slices/uploads';
import { selectIsActivatingAssetNumber, selectVideo } from '../../slices/video';
import { fetchCategories, fetchChannels } from '../../slices/youtube';

function mapStateToProps(state) {
  return {
    video: selectVideo(state),
    isActivatingAssetNumber: selectIsActivatingAssetNumber(state),
    s3Upload: state.s3Upload,
    uploads: state.uploads,
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch) {
  return {
    videoActions: bindActionCreators(
      Object.assign(
        {},
        getVideo,
        saveVideo,
        createAsset,
        revertAsset,
        allDeleteAssetActions
      ),
      dispatch
    ),
    uploadActions: bindActionCreators(
      {
        s3UploadPostProcessing: setS3UploadStatusToPostProcessing,
        s3UploadReset: resetS3UploadState,
        getUploads,
        startVideoUpload,
        startSubtitleFileUpload,
        deleteSubtitle
      },
      dispatch
    ),
    youtubeActions: bindActionCreators(
      { fetchChannels, fetchCategories },
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);
