import React from 'react';
import { Asset } from './VideoAsset';
import VideoUtils from '../../util/video';

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
      this.props.getUploads()
      this.props.setS3UploadPostProcessingStatus(); // reset status to 'post processing'
    }
    if (this.props.uploads.every(upload => {
      return !upload.processing;
    }) && this.props.s3Upload.status === "post-processing") {
      this.props.getVideo(this.props.video.id);
      this.props.resetS3UploadStatus();
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
      const id = this.props.video.id + "-" + upload.id
      if (id !== this.props.s3Upload.id) {
        ret.push(upload);
      }
    });

    return ret;
  };

  render() {
    const deleteAsset = async (asset) => {
      if (asset.id) {
        this.props.deleteAsset(this.props.video, [asset.id]);
      }
      else {
        const assetsToDelete = asset?.sources?.map(source => {
          return source.src;
        });
        if (assetsToDelete?.length > 0) {
          this.props.deleteAsset(this.props.video, assetsToDelete);
        }
      }
    }
    const blocks = this.getAssets().map(upload => {
      return (
        <Asset
          key={upload.id}
          videoId={this.props.video.id}
          upload={upload}
          isActive={parseInt(upload.id) === this.props.activeVersion}
          selectAsset={() => this.props.selectAsset(Number(upload.id))}
          deleteAsset={() => deleteAsset(upload.asset)}
          startSubtitleFileUpload={this.props.startSubtitleFileUpload}
          deleteSubtitle={this.props.deleteSubtitle}
          permissions={this.props.permissions}
        />
      )
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
