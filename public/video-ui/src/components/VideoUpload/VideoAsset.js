import React from 'react';
import Icon from '../Icon';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';

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

function AssetDescription({ title, href, activate }) {
  const button = activate
    ? <button className="button__secondary button__active" onClick={activate}>
        Activate
      </button>
    : false;

  const link = href
    ? <a href={href}><Icon icon="open_in_new" className="icon__assets" /></a>
    : false;

  return (
    <div className="grid__item__title">
      {title}
      {link}
      {button}
    </div>
  );
}

export function Asset({ active, content, caption }) {
  return (
    <div className="grid__item">
      <div className="upload__asset__video">
        {content}
      </div>
      <Overlay active={active} />
      <div className="grid__item__footer">
        {caption}
      </div>
    </div>
  );
}

export function YouTubeAsset({ id, activate }) {
  const content = <YouTubeEmbed id={id} />;

  const caption = (
    <AssetDescription
      title={`ID: ${id}`}
      href={`https://www.youtube.com/watch?v=${id}`}
      activate={activate}
    />
  );

  return <Asset active={!activate} content={content} caption={caption} />;
}

export function SelfHostedAsset({ version, sources, activate }) {
  const content = (
    <video controls>
      {sources.map(source => {
        return (
          <source key={source.src} src={source.src} type={source.mimeType} />
        );
      })}
    </video>
  );
  const caption = (
    <AssetDescription title={`Version ${version}`} activate={activate} />
  );

  return <Asset active={!activate} content={content} caption={caption} />;
}

export function ProcessingAsset({ status, failed, current, total }) {
  const content = failed
    ? UPLOAD_FAILED_MSG
    : <ProgressBar current={current} total={total} />;

  const caption = <AssetDescription title={status} />;

  return <Asset content={content} caption={caption} />;
}
