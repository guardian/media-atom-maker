import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { bindActionCreators } from 'redux';
import { createAsset } from '../../actions/VideoActions/createAsset';
import { getVideo } from '../../actions/VideoActions/getVideo';
import { revertAsset } from '../../actions/VideoActions/revertAsset';
import { saveVideo } from '../../actions/VideoActions/saveVideo';
import PlutoProjectLink from '../../components/Pluto/PlutoProjectLink';
import { PlutoProjectPicker } from '../../components/Pluto/PlutoProjectPicker';
import AddAssetFromURL from '../../components/VideoUpload/AddAssetFromURL';
import AddSelfHostedAsset from '../../components/VideoUpload/AddSelfHostedAsset';
import { VideoTrail } from '../../components/VideoUpload/VideoTrail';
import YoutubeUpload from '../../components/VideoUpload/YoutubeUpload';
import { startVideoUpload } from '../../slices/s3Upload';
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
      s3Upload,
      usage,
      videoEditOpen,
      youtube,
      video: video.video,
      uploads,
      activatingAssetNumber: video.activatingAssetNumber
    })
  );

  useEffect(() => {
    dispatch(getVideo(props.params.id));
    if (store.youtube.categories.length === 0) {
      dispatch(fetchCategories());
    }
    if (store.youtube.channels.length === 0) {
      dispatch(fetchChannels());
    }
  }, [
    dispatch,
    props.params.id,
    store.youtube.categories,
    store.youtube.channels
  ]);

  const uploading = store.s3Upload.status === 'uploading';

  const projectId = store.video.plutoData?.projectId;

  return (
    <div>
      <div className="video__main">
        <div className="video__main__header">
          <div className="video__detailbox">
            <div>
              <div className="form__group">
                {projectId && <PlutoProjectLink projectId={projectId} />}
                <PlutoProjectPicker video={store.video} />
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
            uploads={store.uploads}
            selectAsset={(version: number) =>
              bindActionCreators(revertAsset(store.video.id, version), dispatch)
            }
            permissions={store.config.permissions}
            activatingAssetNumber={store.activatingAssetNumber}
          />
        </div>
      </div>
    </div>
  );
};
