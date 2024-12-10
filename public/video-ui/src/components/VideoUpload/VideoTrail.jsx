import React from 'react';
import { Asset } from './VideoAsset';
import VideoUtils from '../../util/video';

export default class VideoTrail extends React.Component {
  polling = null;

  UNSAFE_componentWillReceiveProps() {
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

    if (this.props.s3Upload.total) {
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
      if (upload.id !== this.props.s3Upload.id) {
        ret.push(upload);
      }
    });

    return ret;
  };

  render() {
    const blocks = this.getAssets().map(upload => (
      <Asset
        key={upload.id}
        upload={upload}
        isActive={parseInt(upload.id) === this.props.activeVersion}
        selectAsset={() => this.props.selectAsset(Number(upload.id))}
        deleteAsset={() => this.props.deleteAsset(this.props.video, upload.asset.id)}
      />
    ));

    const content = blocks.length > 0 ? blocks : false;

    return (
      <div className="video__detail__page__trail">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">Video trail</header>
        </div>
        <div className="grid">
          <div className="grid__list grid__list__trail">
            {content}
          </div>
        </div>
      </div>
    );
  }
}
