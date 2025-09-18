import React from 'react';
import { Asset } from './VideoAsset';
import VideoUtils from '../../util/video';
import {s3UploadPostProcessing} from "../../slices/s3Upload";

export default class VideoTrail extends React.Component {
  polling = null;

  componentWillReceiveProps() {
    const isRecentlyModified = VideoUtils.isRecentlyModified(this.props.video);

    if (!this.polling && isRecentlyModified) {
      this.polling = setInterval(() => this.pollIfRequired(), 5000);
    }
  }

  componentWillUnmount() {
    if (this.polling) {
      clearInterval(this.polling);
    }
  }

  pollIfRequired = () => {
    this.props.uploads.forEach(upload => {
      if (upload.processing) {
        this.props.getUploads();
        return;
      }
    });
  };

  getAssets = () => {
    const ret = [];

    if (this.props.s3Upload.status === "complete") {
      this.props.getUploads();
      this.props.s3UploadPostProcessing(); // reset status to 'post processing'
    }
    if (this.props.uploads.every(upload => {
      return !upload.processing;
    }) && this.props.s3Upload.status === "post-processing") {
      this.props.getVideo(this.props.video.id);
      this.props.s3UploadReset();
    }

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
      const id = this.props.video.id + "-" + upload.id;
      if (id !== this.props.s3Upload.id) {
        ret.push(upload);
      }
    });

    return ret;
  };

  render() {
    const deleteAssetsInUpload = async (asset) => {
      if (asset.id) {
        // if "asset.id" property exists, it should be a Youtube video asset.
        // There should be one asset for Youtube video and we can delete
        // it from the atom with this "asset.id"
        this.props.deleteAssets(this.props.video, [asset.id]);
      }
      else {
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
    const blocks = this.getAssets().map(upload => {
      return (
        <Asset
          key={upload.id}
          videoId={this.props.video.id}
          upload={upload}
          isActive={parseInt(upload.id) === this.props.activeVersion}
          selectAsset={() => {
            if (typeof this.props.activatingAssetNumber=== "number") {
              return;
            }
            return this.props.selectAsset(Number(upload.id));
          }}
          deleteAsset={() => deleteAssetsInUpload(upload.asset)}
          startSubtitleFileUpload={this.props.startSubtitleFileUpload}
          deleteSubtitle={this.props.deleteSubtitle}
          permissions={this.props.permissions}
          activatingAssetNumber={this.props.activatingAssetNumber}
        />
      );
    });

    const content = blocks.length > 0 ? blocks : false;

    return (
      <div className="video__detail__page__trail">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">Video trail</header>
        </div>
        <div className="grid">
          <div className="grid__list grid__list__trail grid__list__wrap">
            {content}
          </div>
        </div>
      </div>
    );
  }
}
