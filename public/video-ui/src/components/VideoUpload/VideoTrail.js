import React from 'react';
import { Asset, buildAssetProps } from './VideoAsset';

export default class VideoTrail extends React.Component {
  polling = null;

  constructor(props) {
    super(props);

    this.polling = setInterval(() => this.pollIfRequired(), 5000);
  }

  componentWillUnmount() {
    clearInterval(this.polling);
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
          status: 'Uploading to S3',
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
    const assets = this.getAssets();

    const blocks = assets.map(upload => {
      const active = upload.id == this.props.activeVersion;
      const props = buildAssetProps(
        upload.id,
        upload.asset,
        upload.processing,
        active,
        this.props.selectAsset
      );

      return <Asset key={upload.id} {...props} />;
    });

    const content = blocks.length > 0
      ? blocks
      : <Asset content="No Assets Uploaded" />;

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
