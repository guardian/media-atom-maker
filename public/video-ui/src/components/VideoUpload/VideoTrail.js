import React from 'react';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import Icon from '../Icon';
import { getProcessingStatus } from '../../services/YoutubeApi';
import _ from 'lodash';

function embed(assetId, platform) {
  if (platform === 'Youtube') {
    return <YouTubeEmbed id={assetId} />;
  } else {
    return false;
  }
}

function youTubeLink(id) {
  return (
    <div>
      <span>Video ID: {id}</span>
      <a href={`https://www.youtube.com/watch?v=${id}`}>
        <Icon icon="open_in_new" className="icon__assets" />
      </a>
    </div>
  );
}

function ErrorAsset({ id, message }) {
  const footer = id
    ? <div className="grid__item__footer">
        <span className="grid__item__title">{youTubeLink(id)}</span>
      </div>
    : false;

  return (
    <div className="grid__item">
      <div className="upload__asset__video upload__asset__empty">
        <span>{message}</span>
      </div>
      {footer}
    </div>
  );
}

function VideoAsset({ id, platform, version, active, selectAsset }) {
  const statusClasses = active
    ? 'publish__label label__live label__frontpage__overlay'
    : 'publish__label label__frontpage__novideo label__frontpage__overlay';
  const status = active ? 'Live' : 'Not Live';

  const selector = !active
    ? <button
        className="button__secondary button__active"
        onClick={() => selectAsset(id, version)}
      >
        Activate
      </button>
    : false;

  return (
    <div className="grid__item">
      <div className="upload__asset__video">{embed(id, platform)}</div>
      <div className="grid__status__overlay">
        <span className={statusClasses}>{status}</span>
      </div>
      <div className="grid__item__footer">
        <span className="grid__item__title">{youTubeLink(id)}</span>
        {selector}
      </div>
    </div>
  );
}

function UploadAsset({ message, total, progress }) {
  const progressBar = total !== undefined && progress !== undefined
    ? <progress className="progress" value={progress} max={total} />
    : <span className="loader" />;

  return (
    <div className="grid__item">
      <div className="upload__asset__video upload__asset__running">
        {progressBar}
      </div>
      <div className="grid__item__footer">
        <span className="grid__item__title">{message}</span>
      </div>
    </div>
  );
}

function FailedUpload({ message }) {
  return (
    <div className="grid__item">
      <div className="upload__asset__video">
        <p>
          <strong>Upload Failed</strong><br />
          You may retry your upload.<br />
          This message will disappear after 10 minutes.
        </p>
      </div>
      <div className="grid__item__footer">
        <span className="grid__item__title">
          <small>
            <code>{message}</code>
          </small>
        </span>
      </div>
    </div>
  );
}

export default class VideoTrail extends React.Component {
  polling = null;
  state = { status: [] };

  constructor(props) {
    super(props);

    this.polling = setInterval(() => this.pollIfRequired(), 5000);
    this.enrichWithStatus(props.assets);
  }

  componentDidUpdate(prevProps) {
    if (!_.isEqual(prevProps.assets, this.props.assets)) {
      this.enrichWithStatus(this.props.assets);
    }
  }

  componentWillUnmount() {
    clearInterval(this.polling);
  }

  pollIfRequired = () => {
    const existingUploads = this.props.uploads.length > 0;
    const missingUploads = this.props.s3Upload.id && !existingUploads;

    if (existingUploads || missingUploads) {
      this.props.getUploads();
      this.props.getVideo();
    }

    if (_.some(this.state.status, entry => entry.status === 'processing')) {
      this.enrichWithStatus(this.props.assets);
    }
  };

  enrichWithStatus = assets => {
    if (assets.length === 0) {
      return;
    }

    const ids = assets.map(asset => asset.id);
    getProcessingStatus(ids).then(resp => {
      this.setState({ status: resp });
    });
  };

  renderS3Upload = () => {
    const total = this.props.s3Upload.total;
    const progress = this.props.s3Upload.progress;

    return (
      <UploadAsset
        key="s3Upload"
        message="Uploading To S3"
        total={total}
        progress={progress}
      />
    );
  };

  renderAsset = asset => {
    const processing = _.find(
      this.state.status,
      status => status.id === asset.id
    );

    if (processing && processing.status === 'processing') {
      const message = processing.timeLeftMs === 0
        ? 'YouTube Processing'
        : `YouTube Processing (${processing.timeLeftMs / 1000}s left)`;

      return <UploadAsset key={asset.id} id={asset.id} message={message} />;
    } else if (processing && processing.status === 'failed') {
      return (
        <ErrorAsset key={asset.id} id={asset.id} message={processing.failure} />
      );
    } else {
      const active = asset.version === this.props.activeVersion;
      const selectable =
        !active &&
        processing &&
        (processing.status === 'succeeded' ||
          processing.status === 'terminated');

      return (
        <VideoAsset
          key={asset.id}
          active={active}
          selectable={selectable}
          selectAsset={this.props.selectAsset}
          {...asset}
        />
      );
    }
  };

  render() {
    const blocks = [];

    if (this.props.s3Upload.total) {
      blocks.push(this.renderS3Upload());
    }

    const uploads = this.props.uploads.filter(
      upload => upload.id !== this.props.s3Upload.id && !upload.assetAdded
    );

    uploads.forEach(upload => {
      blocks.push(
        upload.failed
          ? <FailedUpload key={upload.id} message={upload.status} />
          : <UploadAsset
              key={upload.id}
              message={upload.status}
              total={upload.total}
              progress={upload.current}
            />
      );
    });

    blocks.push(...this.props.assets.map(this.renderAsset));

    const content = blocks.length > 0
      ? blocks
      : <ErrorAsset message="No Assets Uploaded" />;

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
