import React, { useEffect } from 'react';
import PACUpload from '../../components/PACUpload/PACUpload';
import PlutoProjectLink from '../../components/Pluto/PlutoProjectLink';
import { PlutoProjectPicker } from '../../components/Pluto/PlutoProjectPicker';
import AddAssetFromURL from '../../components/VideoUpload/AddAssetFromURL';
import AddSelfHostedAsset from '../../components/VideoUpload/AddSelfHostedAsset';
import VideoTrail from '../../components/VideoUpload/VideoTrail';
import YoutubeUpload from '../../components/VideoUpload/YoutubeUpload';
import { getStore } from '../../util/storeAccessor';

const VideoUpload = props => {
  const hasCategories = () => props.youtube?.categories?.length !== 0;
  const hasChannels = () => props.youtube?.channels?.length !== 0;

  useEffect(() => {
    props.videoActions.getVideo(props.params.id);
    if (!hasCategories()) {
      props.youtubeActions.fetchCategories();
    }
    if (!hasChannels()) {
      props.youtubeActions.fetchChannels();
    }
  }, [props.params.id, props.videoActions, props.youtubeActions]);

  const uploading = props.s3Upload.total > 0;
  const activeVersion = props.video?.activeVersion ?? 0;

  const projectId = props.video?.plutoData?.projectId;

  return (
    <div>
      <div className="video__main">
        <div className="video__main__header">
          <div className="video__detailbox">
            <div>
              <div className="form__group">
                {projectId && <PlutoProjectLink projectId={projectId} />}
                <PlutoProjectPicker
                  video={props.video || {}}
                  saveVideo={props.videoActions.saveVideo}
                />
              </div>
            </div>
            <YoutubeUpload
              video={props.video || {}}
              categories={props.youtube.categories}
              channels={props.youtube.channels}
              uploading={uploading}
              saveVideo={props.videoActions.saveVideo}
              startUpload={props.uploadActions.startVideoUpload}
            />
            <PACUpload
              startUpload={props.uploadActions.startPacFileUpload}
              video={props.video}
            />
            <AddAssetFromURL
              video={props.video}
              createAsset={props.videoActions.createAsset}
            />
            <AddSelfHostedAsset
              video={props.video || {}}
              permissions={getStore().getState().config.permissions}
              uploading={uploading}
              startUpload={props.uploadActions.startVideoUpload}
            />
          </div>
          <VideoTrail
            video={props.video}
            activeVersion={activeVersion}
            s3Upload={props.s3Upload}
            uploads={props.uploads}
            deleteAssets={props.videoActions.deleteAssets}
            selectAsset={version =>
              props.videoActions.revertAsset(props.video.id, version)
            }
            getUploads={() => {
              props.uploadActions.getUploads(props.video.id);
            }}
            startSubtitleFileUpload={
              props.uploadActions.startSubtitleFileUpload
            }
            deleteSubtitle={props.uploadActions.deleteSubtitle}
            permissions={getStore().getState().config.permissions}
            s3UploadPostProcessing={props.uploadActions.s3UploadPostProcessing}
            s3UploadReset={props.uploadActions.s3UploadReset}
            activatingAssetNumber={props.saveState?.activatingAssetNumber}
            getVideo={props.videoActions.getVideo}
          />
        </div>
      </div>
    </div>
  );
};

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getUpload from '../../actions/UploadActions/getUploads';
import * as s3UploadActions from '../../actions/UploadActions/s3Upload';
import * as createAsset from '../../actions/VideoActions/createAsset';
import * as allDeleteAssetActions from '../../actions/VideoActions/deleteAsset';
import * as getVideo from '../../actions/VideoActions/getVideo';
import * as revertAsset from '../../actions/VideoActions/revertAsset';
import * as saveVideo from '../../actions/VideoActions/saveVideo';
import {
  resetS3UploadState,
  setS3UploadStatusToPostProcessing
} from '../../slices/s3Upload';
import { selectVideo } from '../../slices/video';
import { fetchCategories, fetchChannels } from '../../slices/youtube';

function mapStateToProps(state) {
  return {
    video: selectVideo(state),
    s3Upload: state.s3Upload,
    uploads: state.uploads,
    youtube: state.youtube,
    saveState: state.saveState
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
      Object.assign(
        {
          s3UploadPostProcessing: setS3UploadStatusToPostProcessing,
          s3UploadReset: resetS3UploadState
        },
        s3UploadActions,
        getUpload
      ),
      dispatch
    ),
    youtubeActions: bindActionCreators(
      { fetchChannels, fetchCategories },
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoUpload);
