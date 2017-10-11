import React from 'react';
import moment from 'moment';
import Icon from '../Icon';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import { VideoEmbed } from '../utils/VideoEmbed';

function presenceInitials(email) {
  const emailParts = email.split('@');
  const names = [];

  if (emailParts.length < 2) {
    names.push(emailParts[0]);
  } else {
    const nameParts = emailParts[0].split('.');
    names.push(...nameParts);
  }

  const initials = names.map(name => name.toUpperCase()[0]);

  return initials.join('');
}

function ProgressBar({ current, total }) {
  return total !== undefined && current !== undefined
    ? <progress className="progress" value={current} max={total} />
    : <span className="loader" />;
}

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
  const initials = presenceInitials(user);
  const startDate = moment(startTimestamp).format('YYYY/MM/DD HH:mm:ss');

  return (
    <div className="upload__asset__metadata">
      <div className="upload__asset__filename" title={originalFilename}>
        {originalFilename}
      </div>
      <div className="upload__asset__time">
        {startDate}
        <ul className="presence-list">
          <li className="presence-list__user" title={user}>
            {initials}
          </li>
        </ul>
      </div>
    </div>
  );
}

function YouTubeVideo({ id, active }) {
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

function SelfHostedVideo({ sources, active }) {
  return (
    <div className="upload__asset__video">
      <VideoEmbed sources={sources} />
      <Overlay active={active} />
    </div>
  );
}

function UploadFailed({ msg }) {
  return (
    <div className="upload__asset__video">
      <p>
        <strong>Upload Failed</strong>
        <br />
        {msg}
      </p>
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

export function Asset({ upload, active, selectAsset }) {
  const controls = (
    <AssetControls
      id={upload.asset.id}
      active={active}
      metadata={upload.metadata}
      selectAsset={selectAsset}
    />
  );

  let top = false;
  let bottom = false;

  if (upload.processing && upload.processing.failed) {
    top = <UploadFailed msg={upload.processing.statu} />;
  } else if (upload.processing) {
    top = <ProgressBar {...upload.processing} />;
    bottom = upload.processing.status;
  } else if (upload.asset && upload.asset.id) {
    top = <YouTubeVideo id={upload.asset.id} active={active} />;
    bottom = controls;
  } else if (upload.asset.sources) {
    top = <SelfHostedVideo sources={upload.asset.sources} active={active} />;
    bottom = controls;
  }

  return (
    <div className="grid__item">
      {top}
      {bottom}
    </div>
  );
}
