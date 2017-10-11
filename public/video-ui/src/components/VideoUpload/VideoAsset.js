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
        Active
      </span>
    </div>
  );
}

function YouTubeVideo({ id, active }) {
  const youTubeLink = `https://www.youtube.com/watch?v=${id}`;

  return (
    <div className="upload">
      <YouTubeEmbed id={id} />
      <Overlay active={active} />
      <a
        className="upload__link"
        href={youTubeLink}
        target="_blank"
        rel="noopener noreferrer"
      >
        <Icon icon="open_in_new" className="icon__assets" />
      </a>
    </div>
  );
}

function SelfHostedVideo({ sources, active }) {
  return (
    <div className="upload">
      <VideoEmbed sources={sources} />
      <Overlay active={active} />
    </div>
  );
}

function UploadFailed() {
  return (
    <div className="upload">
      <p>
        <strong>Upload Failed</strong>
      </p>
    </div>
  );
}

function AssetControls({ upload, active, selectAsset }) {
  let fileInfo = upload.asset && upload.asset.id ? upload.asset.id : upload.id;
  let userInfo = false;
  let activateButton = false;

  if (upload.metadata) {
    const { user, startTimestamp, originalFilename } = upload.metadata;

    const initials = presenceInitials(user);
    const startDate = moment(startTimestamp).format('YYYY/MM/DD HH:mm:ss');

    fileInfo = (
      <div className="upload__metadata">
        <div className="upload__filename" title={originalFilename}>
          {originalFilename}
        </div>
        <div className="upload__time">
          <small>{startDate}</small>
        </div>
      </div>
    );

    userInfo = (
      <ul className="presence-list">
        <li className="presence-list__user" title={user}>
          {initials}
        </li>
      </ul>
    );
  }

  if (!active) {
    activateButton = (
      <button className="btn" onClick={selectAsset}>
        Activate
      </button>
    );
  }

  return (
    <div className="upload__actions">
      {fileInfo}
      {userInfo}
      {activateButton}
    </div>
  );
}

export function Asset({ upload, active, selectAsset }) {
  let top = false;
  let bottom = false;

  if (upload.processing) {
    top = (
      <div className="upload">
        {upload.processing.failed
          ? <UploadFailed />
          : <ProgressBar {...upload.processing} />}
      </div>
    );

    bottom = (
      <div className="upload__actions">
        {upload.processing.status}
      </div>
    );
  } else if (upload.asset) {
    top = upload.asset.id
      ? <YouTubeVideo id={upload.asset.id} active={active} />
      : <SelfHostedVideo sources={upload.asset.sources} active={active} />;

    bottom = (
      <AssetControls
        upload={upload}
        active={active}
        selectAsset={selectAsset}
      />
    );
  }

  return (
    <div className="grid__item">
      {top}
      <div className="grid__item__footer">
        {bottom}
      </div>
    </div>
  );
}
