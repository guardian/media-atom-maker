import React, { useEffect } from 'react';
import { Asset } from './VideoAsset';
import { Video } from '../../services/VideosApi';
import { S3UploadState, Upload } from '../../slices/s3Upload';
import { useSelector } from 'react-redux';
import { RootState } from '../../util/setupStore';


// video={props.video}
//             activeVersion={activeVersion}
//             s3Upload={props.s3Upload}
//             uploads={props.uploads}
//             deleteAssets={bindActionCreators(deleteAssets, dispatch)}
//             selectAsset={(version: number) =>
//               bindActionCreators(revertAsset(props.video.id, version), dispatch)
//             }
//             getUploads={() => {
//               bindActionCreators(getUploads(props.video.id), dispatch);
//             }}
//             startSubtitleFileUpload={bindActionCreators(
//               startSubtitleFileUpload,
//               dispatch
//             )}
//             deleteSubtitle={bindActionCreators(deleteSubtitle, dispatch)}
//             permissions={props.config.permissions}
//             s3UploadPostProcessing={bindActionCreators(
//               setS3UploadStatusToPostProcessing,
//               dispatch
//             )}
//             s3UploadReset={bindActionCreators(resetS3UploadState, dispatch)}
//             activatingAssetNumber={props.activatingAssetNumber}
//             getVideo={bindActionCreators(getVideo, dispatch)}
type Props = {
  video: Video;
  s3Upload: S3UploadState;
  uploads: Upload[];
  deleteAssets: (video: Video, assetIds: string[]) => void;
  selectAsset: (version: number) => void;
  getUploads: () => void;
  startSubtitleFileUpload: (input: {
    file: File;
    id: string;
    version: string;
  }) => void;
  deleteSubtitle: (input: { id: string; version: string }) => void;
  permissions: Record<string, boolean>;
  s3UploadPostProcessing: () => void;
  s3UploadReset: () => void;
  activatingAssetNumber: number;
  getVideo: (id: string) => void;
}


export const VideoTrail = (props: Props) => {
  const [assets, setAssets] = React.useState<Upload[]>([]);


  useEffect(() => {
    // set up polling on mount
    const pollingInterval = 5000; // 5 seconds
const pollIfRequired = () => {
    props.uploads.forEach(upload => {
      if (upload.processing) {
        props.getUploads();
        return;
      }
    });
  };

    const polling = setInterval(() => {
      pollIfRequired();
    }, pollingInterval);

    return () => {
      clearInterval(polling);
    };
  }, [props.uploads, props.s3Upload]);

  componentDidUpdate(prevProps) {
    // Handle upload completion
    if (this.props.s3Upload.status === 'complete') {
      this.props.getUploads();
      this.props.s3UploadPostProcessing(); // reset status to 'post processing'
    }

    // Handle post-processing completion
    if (
      this.props.uploads.every(upload => !upload.processing) &&
      this.props.s3Upload.status === 'post-processing'
    ) {
      this.props.getVideo(this.props.video.id);
      this.props.s3UploadReset();
    }
  }

  componentWillUnmount() {
    if (this.polling) {
      clearInterval(this.polling);
    }
  }

  

  getAssets = () => {
    const ret = [];

    if (this.props.s3Upload.total) {
      // create an item to represent the current upload
      ret.push({
        id: 's3Upload',
        processing: {
          status: 'Starting upload...',
          failed: false,
          current: this.props.s3Upload.progress,
          total: this.props.s3Upload.total
        }
      });
    }

    this.props.uploads.forEach(upload => {
      // prevent duplication by omitting currently uploading item
      // s3Upload.id: <atomId>-<version>
      // upload.id: <version>
      const id = this.props.video.id + '-' + upload.id;
      if (id !== this.props.s3Upload.id) {
        ret.push(upload);
      }
    });

    return ret;
  };

    const deleteAssetsInUpload = async (asset: Upload) => {
      if (asset.id) {
        // if "asset.id" property exists, it should be a Youtube video asset.
        // There should be one asset for Youtube video and we can delete
        // it from the atom with this "asset.id"
        deleteAssets(props.video, [asset.id]);
      } else {
        // if "asset.id" property does not exist, it should be a self-hosting
        // video asset.  There may be multiple assets for a self-hosted video.
        // We can extract the asset IDs from the "src" property of each member
        // of the "sources" property.
        const assetsToDelete = asset?.sources?.map(source => source.src);
        if (assetsToDelete?.length > 0) {
          this.props.deleteAssets(this.props.video, assetsToDelete);
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
            {assets.map(upload => 
              <Asset
          key={upload.id}
          videoId={props.video.id}
          upload={upload}
          isActive={parseInt(upload.id) === props.video.activeVersion}
          selectAsset={() => {
            if (typeof props.activatingAssetNumber === 'number') {
              return;
            }
            return selectAsset(Number(upload.id));
          }}
          deleteAsset={() => deleteAssetsInUpload(upload.asset)}
          startSubtitleFileUpload={startSubtitleFileUpload}
          deleteSubtitle={deleteSubtitle}
          permissions={permissions}
          activatingAssetNumber={activatingAssetNumber}
        />
            )}
          </div>
        </div>
      </div>
    );
  
}