import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { bindActionCreators } from 'redux';
import { createAsset } from '../../actions/VideoActions/createAsset';
import { deleteAssets } from '../../actions/VideoActions/deleteAsset';
import { getVideo } from '../../actions/VideoActions/getVideo';
import { revertAsset } from '../../actions/VideoActions/revertAsset';
import { saveVideo } from '../../actions/VideoActions/saveVideo';
import PACUpload from '../../components/PACUpload/PACUpload';
import PlutoProjectLink from '../../components/Pluto/PlutoProjectLink';
import { PlutoProjectPicker } from '../../components/Pluto/PlutoProjectPicker';
import AddAssetFromURL from '../../components/VideoUpload/AddAssetFromURL';
import AddSelfHostedAsset from '../../components/VideoUpload/AddSelfHostedAsset';
import VideoTrail from '../../components/VideoUpload/VideoTrail';
import YoutubeUpload from '../../components/VideoUpload/YoutubeUpload';
import {
  deleteSubtitle,
  resetS3UploadState,
  setS3UploadStatusToPostProcessing,
  startPacFileUpload,
  startSubtitleFileUpload,
  startVideoUpload
} from '../../slices/s3Upload';
import { getUploads } from '../../slices/uploads';
import { fetchCategories, fetchChannels } from '../../slices/youtube';
import { AppDispatch, RootState } from '../../util/setupStore';
import { getStore } from '../../util/storeAccessor';

export const VideoUpload = (props: { params: { id: string } }) => {
  const dispatch = useDispatch<AppDispatch>();

  const store = useSelector(
    ({
      config,
      error,
      formFieldsWarning,
      search,
      saveState,
      s3Upload,
      usage,
      videoEditOpen,
      youtube,
      video,
      uploads
    }: RootState) => ({
      config,
      error,
      formFieldsWarning,
      search,
      saveState,
      s3Upload,
      usage,
      videoEditOpen,
      youtube,
      video: video.video,
      uploads
    })
  );

  const hasCategories = () => store.youtube.categories?.length !== 0;
  const hasChannels = () => store.youtube.channels?.length !== 0;

  useEffect(() => {
    dispatch(getVideo(props.params.id));
    if (!hasCategories()) {
      dispatch(fetchCategories());
    }
    if (!hasChannels()) {
      dispatch(fetchChannels());
    }
  }, [props.params.id]);

  const uploading = store.s3Upload.total > 0;
  const activeVersion = store.video.activeVersion ?? 0;

  const projectId = store.video.plutoData?.projectId;

  function dispatchSaveVideo(video: typeof store.video) {
    return dispatch(saveVideo(video));
  }
  const boundSaveVideo = bindActionCreators(dispatchSaveVideo, dispatch);

  return (
    <div>
      <div className="video__main">
        <div className="video__main__header">
          <div className="video__detailbox">
            <div>
              <div className="form__group">
                {projectId && <PlutoProjectLink projectId={projectId} />}
                <PlutoProjectPicker
                  video={store.video}
                  saveVideo={dispatchSaveVideo}
                />
              </div>
            </div>
            <YoutubeUpload
              video={store.video}
              categories={store.youtube.categories}
              channels={store.youtube.channels}
              uploading={uploading}
              saveVideo={bindActionCreators(saveVideo, dispatch)}
              startUpload={bindActionCreators(startVideoUpload, dispatch)}
            />
            <PACUpload
              startUpload={bindActionCreators(startPacFileUpload, dispatch)}
              video={store.video}
            />
            <AddAssetFromURL
              video={store.video}
              createAsset={bindActionCreators(createAsset, dispatch)}
            />
            <AddSelfHostedAsset
              video={store.video}
              permissions={getStore().getState().config.permissions}
              uploading={uploading}
              startUpload={bindActionCreators(startVideoUpload, dispatch)}
            />
          </div>
          <VideoTrail
            video={store.video}
            activeVersion={activeVersion}
            s3Upload={store.s3Upload}
            uploads={store.uploads}
            deleteAssets={bindActionCreators(deleteAssets, dispatch)}
            selectAsset={(version: number) =>
              bindActionCreators(revertAsset(store.video.id, version), dispatch)
            }
            getUploads={() => {
              bindActionCreators(getUploads(store.video.id), dispatch);
            }}
            startSubtitleFileUpload={bindActionCreators(
              startSubtitleFileUpload,
              dispatch
            )}
            deleteSubtitle={bindActionCreators(deleteSubtitle, dispatch)}
            permissions={getStore().getState().config.permissions}
            s3UploadPostProcessing={bindActionCreators(
              setS3UploadStatusToPostProcessing,
              dispatch
            )}
            s3UploadReset={bindActionCreators(resetS3UploadState, dispatch)}
            activatingAssetNumber={store.saveState?.activatingAssetNumber}
            getVideo={bindActionCreators(getVideo, dispatch)}
          />
        </div>
      </div>
    </div>
  );
};
