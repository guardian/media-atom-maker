import React from 'react';
import moment from 'moment';
import Icon from '../Icon';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import { VideoEmbed } from '../utils/VideoEmbed';

function Overlay({ active }) {
  if (!active) {
    return false;
  }

  return (
    <div className="grid__status__overlay">
      <span className="publish__label label__live label__frontpage__overlay">
        Live
      </span>
    </div>
  );
}

function Metadata({ user, startTimestamp, originalFilename }) {
  const startDate = moment(startTimestamp).format('YYYY/MM/DD HH:mm:ss');

  return (
    <div className="upload__asset__metadata">
      <div>{originalFilename}.BLAjghrbhebhhebhevhwbhvhjwbvhrhbhb</div>
      <div>
        <small>
          {startDate}
        </small>
      </div>
    </div>
  );
}

function YouTubeVideo({ id, active, selectAsset }) {
  const youTubeLink = `https://www.youtube.com/watch?v=${id}`;

  return (
    <div className="upload__asset__video">
      <YouTubeEmbed id={id} />
      <Overlay active={active} />
      <a href={youTubeLink} target="_blank" rel="noopener noreferrer">
        <Icon icon="open_in_new" className="icon__assets" />
      </a>
    </div>
  );
}

function AssetControls({ id, active, metadata, selectAsset }) {
  return (
    <div className="grid__item__footer">
      <div className="upload__asset__actions">
        {metadata ? <Metadata {...metadata} /> : id}
        <button className="btn" onClick={selectAsset}>
          Activate
        </button>
      </div>
    </div>
  );
}

export function Asset2({ upload, active, selectAsset }) {
  let top = false;
  let bottom = false;

  if (upload.asset && upload.asset.id) {
    top = <YouTubeVideo id={upload.asset.id} />;
    bottom = (
      <AssetControls
        id={upload.asset.id}
        active={active}
        metadata={upload.metadata}
        selectAsset={selectAsset}
      />
    );
  }

  return (
    <div className="grid__item">
      {top}
      {bottom}
    </div>
  );
}

function UploadFailed({ msg }) {
  return (
    <p>
      <strong>Upload Failed</strong>
      <br />
      {msg}
    </p>
  );
}

function ProgressBar({ current, total }) {
  return total !== undefined && current !== undefined
    ? <progress className="progress" value={current} max={total} />
    : <span className="loader" />;
}

export function Asset(props) {
  const button = props.activateFn
    ? <button
        className="button__secondary button__active"
        onClick={props.activateFn}
      >
        Activate
      </button>
    : false;

  const link = props.href
    ? <a href={props.href} target="_blank" rel="noopener noreferrer">
        <Icon icon="open_in_new" className="icon__assets" />
      </a>
    : false;

  return (
    <div className="grid__item">
      <div className="upload__asset__video">
        {props.content}
        {link}
      </div>
      <Overlay active={props.active} />
      <div className="grid__item__footer">
        <div className="grid__item__title">
          {props.title}
          {button}
        </div>
      </div>
    </div>
  );
}

function buildTitle(id, asset, processing, metadata) {
  if (processing && !processing.failed) {
    return processing.status;
  } else if (metadata) {
    const { startTimestamp, user, originalFilename } = metadata;
    const startDate = moment(startTimestamp);

    return (
      <div className="upload__asset__metadata">
        <strong>
          {startDate.format('DD/MM/YY HH:mm:ss')}
        </strong>
        <br />
        <small>{user}</small>
      </div>
    );
  } else if (asset) {
    return `ID: ${asset.id}`;
  } else {
    return `Version ${id}`;
  }
}

export function buildAssetProps(upload, active, selectAsset) {
  const { id, asset, metadata, processing } = upload;
  const title = buildTitle(id, asset, processing, metadata);

  if (processing) {
    return {
      title,
      content: processing.failed
        ? <UploadFailed msg={processing.status} />
        : <ProgressBar {...processing} />
    };
  } else if (asset.id) {
    return {
      title,
      active,
      href: `https://www.youtube.com/watch?v=${asset.id}`,
      content: <YouTubeEmbed id={asset.id} />,
      activateFn: active ? null : () => selectAsset(Number(id))
    };
  } else {
    return {
      title,
      active,
      content: <VideoEmbed sources={asset.sources} />,
      activateFn: active ? null : () => selectAsset(Number(id))
    };
  }
}
