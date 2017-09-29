import React from 'react';
import Icon from '../Icon';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import { VideoEmbed } from '../utils/VideoEmbed';

const UPLOAD_FAILED_MSG = (
  <p>
    <strong>Upload Failed</strong><br />
    You may retry your upload.<br />
    This message will disappear after 10 minutes.
  </p>
);

function ProgressBar({ current, total }) {
  return total !== undefined && current !== undefined
    ? <progress className="progress" value={current} max={total} />
    : <span className="loader" />;
}

function Overlay({ active }) {
  if (active === undefined) {
    return false;
  }

  const statusClass = active
    ? 'publish__label label__live label__frontpage__overlay'
    : 'publish__label label__frontpage__novideo label__frontpage__overlay';

  const status = active ? 'Live' : 'Not Live';

  return (
    <div className="grid__status__overlay">
      <span className={statusClass}>{status}</span>
    </div>
  );
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
      </div>
      <Overlay active={props.active} />
      <div className="grid__item__footer">
        <div className="grid__item__title">
          {props.title}<br />{props.description}
          {link}
          {button}
        </div>
      </div>
    </div>
  );
}

function buildTitle(id, asset, processing, metadata) {
  if (processing) {
    return `ID: ${asset.id}`;
  }

  if (metadata) {
    const startDate = new Date(metadata.startTimestamp);

    return (
      <div>
        {metadata.originalFilename}<br />
        {`${startDate.getDay()}/${startDate.getMonth()}/${startDate.getFullYear()} ${startDate.getHours()}:${startDate.getMinutes()}:${startDate.getSeconds()}`}
      </div>
    );
  }

  if (asset.id) {
    return `ID: ${asset.id}`;
  }

  return `Version ${id}`;
}

export function buildAssetProps(upload, active, selectAsset) {
  const { id, asset, metadata, processing } = upload;
  const title = buildTitle(id, asset, processing, metadata);

  if (processing) {
    return {
      title,
      content: processing.failed
        ? UPLOAD_FAILED_MSG
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
