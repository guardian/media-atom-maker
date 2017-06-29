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

export function Asset({ content, title, href, active, activateFn }) {
  const button = activateFn
    ? <button className="button__secondary button__active" onClick={activateFn}>
        Activate
      </button>
    : false;

  const link = href
    ? <a href={href} target="_blank" rel="noopener noreferrer">
        <Icon icon="open_in_new" className="icon__assets" />
      </a>
    : false;

  return (
    <div className="grid__item">
      <div className="upload__asset__video">
        {content}
      </div>
      <Overlay active={active} />
      <div className="grid__item__footer">
        <div className="grid__item__title">
          {title}
          {link}
          {button}
        </div>
      </div>
    </div>
  );
}

export function buildAssetProps(id, asset, processing, active, selectAsset) {
  if (processing) {
    return {
      title: processing.status,
      content: processing.failed
        ? UPLOAD_FAILED_MSG
        : <ProgressBar {...processing} />
    };
  } else if (asset.id) {
    return {
      title: `ID: ${asset.id}`,
      active,
      href: `https://www.youtube.com/watch?v=${asset.id}`,
      content: <YouTubeEmbed id={asset.id} />,
      activateFn: active ? null : () => selectAsset(Number(id))
    };
  } else {
    return {
      title: `Version ${id}`,
      active,
      content: <VideoEmbed sources={asset.sources} />,
      activateFn: active ? null : () => selectAsset(Number(id))
    };
  }
}
