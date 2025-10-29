import React, { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { deleteAssets } from '../../actions/VideoActions/deleteAsset';
import { getVideo } from '../../actions/VideoActions/getVideo';
import { Video } from '../../services/VideosApi';
import { Upload } from '../../slices/s3Upload';
import { getUploads } from '../../slices/uploads';
import { AppDispatch } from '../../util/setupStore';
import { Asset } from './VideoAsset';

type Props = {
  video: Video;
  uploads: Upload[];
  selectAsset: (version: number) => void;
  permissions: Record<string, boolean>;
  activatingAssetNumber: number;
};

export const VideoTrail = ({
  video,
  uploads,
  selectAsset,
  permissions,
  activatingAssetNumber
}: Props) => {
  const dispatch = useDispatch<AppDispatch>();

  const pollingInterval = 5000;

  useEffect(() => {
    let polling: NodeJS.Timeout | null = null;
    if (
      uploads.some(upload => upload.processing && !upload.processing.failed)
    ) {
      polling = setInterval(() => {
        dispatch(getUploads(video.id));
      }, pollingInterval);
    } else if (!!video.id) {
      dispatch(getVideo(video.id));
    }
    return () => {
      if (polling) {
        clearInterval(polling);
      }
    };
  }, [dispatch, uploads, video.id]);

  const deleteAssetsInUpload = async (asset: Upload['asset']) => {
    if (asset.id) {
      // if "asset.id" property exists, it should be a Youtube video asset.
      // There should be one asset for Youtube video and we can delete
      // it from the atom with this "asset.id"
      dispatch(deleteAssets(video, [asset.id]));
    } else {
      // if "asset.id" property does not exist, it should be a self-hosting
      // video asset.  There may be multiple assets for a self-hosted video.
      // We can extract the asset IDs from the "src" property of each member
      // of the "sources" property.
      const assetsToDelete = asset?.sources?.map(source => source.src);
      if (assetsToDelete?.length > 0) {
        dispatch(deleteAssets(video, assetsToDelete));
      }
    }
  };

  return (
    <div className="video__detail__page__trail">
      <div className="video__detailbox__header__container">
        <header className="video__detailbox__header">Video trail</header>
      </div>
      <div className="grid">
        <div className="grid__list grid__list__trail grid__list__wrap">
          {uploads.map(upload => (
            <Asset
              key={upload.id}
              videoId={video.id}
              upload={upload}
              isActive={parseInt(upload.id) === video.activeVersion}
              selectAsset={() => {
                if (typeof activatingAssetNumber === 'number') {
                  return;
                }
                return selectAsset(Number(upload.id));
              }}
              deleteAsset={() => deleteAssetsInUpload(upload.asset)}
              permissions={permissions}
              activatingAssetNumber={activatingAssetNumber}
            />
          ))}
        </div>
      </div>
    </div>
  );
};
