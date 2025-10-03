import React, { useEffect } from 'react';
import PACUpload from '../../components/PACUpload/PACUpload';
import PlutoProjectLink from '../../components/Pluto/PlutoProjectLink';
import { PlutoProjectPicker } from '../../components/Pluto/PlutoProjectPicker';
import AddAssetFromURL from '../../components/VideoUpload/AddAssetFromURL';
import AddSelfHostedAsset from '../../components/VideoUpload/AddSelfHostedAsset';
import VideoTrail from '../../components/VideoUpload/VideoTrail';
import YoutubeUpload from '../../components/VideoUpload/YoutubeUpload';
import { getStore } from '../../util/storeAccessor';

type Props = PropsFromRedux & {
  params: { id: string };
};

const VideoUpload = (props: Props) => {
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
                  video={props.video}
                  saveVideo={props.videoActions.saveVideo}
                />
              </div>
            </div>
            <YoutubeUpload
              video={props.video}
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
              video={props.video}
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
            selectAsset={(version: number) =>
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
import { connect, ConnectedProps } from 'react-redux';
import { bindActionCreators } from 'redux';
import { getUploads } from '../../actions/UploadActions/getUploads';
import { s3UploadActions } from '../../actions/UploadActions/s3Upload';
import { createAsset } from '../../actions/VideoActions/createAsset';
import { allDeleteAssetActions } from '../../actions/VideoActions/deleteAsset';
import { getVideo } from '../../actions/VideoActions/getVideo';
import { revertAsset } from '../../actions/VideoActions/revertAsset';
import { saveVideo } from '../../actions/VideoActions/saveVideo';
import {
  resetS3UploadState,
  setS3UploadStatusToPostProcessing
} from '../../slices/s3Upload';
import { selectVideo } from '../../slices/video';
import { fetchCategories, fetchChannels } from '../../slices/youtube';
import { AppDispatch, RootState } from '../../util/setupStore';

function mapStateToProps(state: RootState) {
  return {
    video: selectVideo(state),
    s3Upload: state.s3Upload,
    uploads: state.uploads,
    youtube: state.youtube,
    saveState: state.saveState
  };
}

function mapDispatchToProps(dispatch: AppDispatch) {
  return {
    videoActions: bindActionCreators(
      {
        getVideo,
        saveVideo,
        createAsset,
        revertAsset,
        ...allDeleteAssetActions
      },
      dispatch
    ),
    uploadActions: bindActionCreators(
      {
        s3UploadPostProcessing: setS3UploadStatusToPostProcessing,
        s3UploadReset: resetS3UploadState,
        ...s3UploadActions,
        getUploads
      },
      dispatch
    ),
    youtubeActions: bindActionCreators(
      { fetchChannels, fetchCategories },
      dispatch
    )
  };
}

/**
 * Generating the connector first to make it easier to extract the props,
 * @see https://react-redux.js.org/using-react-redux/usage-with-typescript#typing-the-connect-higher-order-component
 */
const connector = connect(mapStateToProps, mapDispatchToProps);

type PropsFromRedux = ConnectedProps<typeof connector>;

export default connector(VideoUpload);
