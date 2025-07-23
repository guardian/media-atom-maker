import React from 'react';
import moment from 'moment';
import Icon from '../Icon';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import { VideoEmbed } from '../utils/VideoEmbed';
import DeleteButton from '../DeleteButton';

type RuntimeUploadMetadata = YouTubeUploadMetadata | SelfHostedUploadMetadata;

type YouTubeUploadMetadata = {
  type: 'YouTubeUploadMetadata';
  channel: string;
  uri?: string;
};

type SelfHostedUploadMetadata = {
  type: 'SelfHostedUploadMetadata';
  jobs: string[];
};

type PlutoSyncMetadataMessage = {
  type: string;
  projectId?: string;
  s3Key: string;
  atomId: string;
  title: string;
  user: string;
  posterImageUrl?: string;
};

type UploadMetaData = {
  user: string;
  bucket: string;
  region: string;
  title: string;
  pluto: PlutoSyncMetadataMessage;
  selfHost: boolean;
  runtime: RuntimeUploadMetadata;
  originalFilename?: string;
  version?: number;
  startTimestamp?: number;
};

export type VideoProcessing = {
  status: string;
  failed: boolean;
  current?: string | number | readonly string[];
  total?: string | number;
};

export type VideoUpload = {
  id: string;
  processing: VideoProcessing;
  metadata: UploadMetaData;
  asset: VideoAsset;
};

export type VideoSource = {
  src: string;
  mimeType: string;
};

export type YouTubeAsset = {
  type: 'YouTubeAsset';
  id: string;
};

export type SelfHostedAsset = {
  type: 'SelfHostedAsset';
  sources: VideoSource[];
};

export type VideoAsset = YouTubeAsset | SelfHostedAsset;

function presenceInitials(email: string) {
  const emailParts = email.split('@');
  const names = [];

  if (emailParts.length < 2) {
    names.push(emailParts[0]);
  } else {
    const nameParts = emailParts[0].split('.');
    names.push(...nameParts.slice(0, 2));
  }

  const initials = names.map(name => name.toUpperCase()[0]);

  return initials.join('');
}

function AssetControls({
  user,
  children,
  isActive,
  selectAsset,
  deleteAsset
}: {
  user: string;
  children: JSX.Element;
  isActive: boolean;
  selectAsset: () => void;
  deleteAsset: () => void;
}) {
  const userCircle = (
    <ul className="presence-list">
      <li className="presence-list__user" title={user}>
        {presenceInitials(user)}
      </li>
    </ul>
  );

  const activateButton = (
    <button className="btn upload__activate-btn" onClick={selectAsset}>
      Activate
    </button>
  );

  const deleteButton = (
    <DeleteButton
      tooltip="Remove asset from Atom (does not affect YouTube.com)"
      onDelete={deleteAsset}
    />
  );

  return (
    <div className="upload__actions">
      {children}
      <div className="upload__right">
        {user && userCircle}
        {!isActive && activateButton}
        {!isActive && deleteButton}
      </div>
    </div>
  );
}

function AssetInfo({ info, timestamp }: { info: string; timestamp?: number }) {
  const dateTime = timestamp && moment(timestamp).format('YYYY/MM/DD HH:mm:ss');

  return (
    <div className="upload__left">
      <div className="upload__info" title={info}>
        {info}
      </div>
      <div>
        <small>{dateTime}</small>
      </div>
    </div>
  );
}

function AssetDisplay({
  id,
  isActive,
  sources
}: {
  id?: string;
  isActive: boolean;
  sources: VideoSource[];
}) {
  const embed = id ? (
    <YouTubeEmbed id={id} largePreview={true} />
  ) : (
    <VideoEmbed sources={sources} />
  );

  return (
    <div className="upload">
      {embed}
      {id && (
        <a
          className={'upload__link'}
          href={`https://www.youtube.com/watch?v=${id}`}
          target={'_blank'}
          rel={'noopener noreferrer'}
        >
          <Icon icon="open_in_new" className="icon__assets" />
        </a>
      )}
      {isActive && (
        <div className="grid__status__overlay">
          <span className="publish__label label__live label__frontpage__overlay">
            Active
          </span>
        </div>
      )}
    </div>
  );
}

function AssetProgress({
  failed,
  current,
  total
}: {
  failed: boolean;
  current?: string | number | readonly string[];
  total?: string | number;
}) {
  if (failed) {
    return (
      <div className="upload">
        <p>
          <strong>Upload Failed</strong>
        </p>
      </div>
    );
  }
  if (total !== undefined && current !== undefined) {
    return <progress className="progress" value={current} max={total} />;
  }
  return <span className="loader" />;
}

export function Asset({
  upload,
  isActive,
  selectAsset,
  deleteAsset
}: {
  upload: VideoUpload;
  isActive: boolean;
  selectAsset: () => void;
  deleteAsset: () => void;
}) {
  console.log(typeof upload, { upload });

  const { asset, metadata, processing } = upload;
  const user = metadata.user;
  const info = metadata?.originalFilename || `Version ${upload.id}`;
  const timestamp = metadata?.startTimestamp;

  if (processing) {
    return (
      <div className="grid__item">
        <div className="upload">
          <AssetProgress {...processing} />
        </div>
        <div className="grid__item__footer">
          <AssetControls
            user={user}
            isActive={false}
            selectAsset={selectAsset}
            deleteAsset={deleteAsset}
          >
            <AssetInfo info={processing.status} />
          </AssetControls>
        </div>
      </div>
    );
  }

  if (asset) {
    return (
      <div className="grid__item">
        <AssetDisplay
          isActive={isActive}
          id={asset.type === 'YouTubeAsset' && asset.id}
          sources={asset.type === 'SelfHostedAsset' && asset.sources}
        />
        <div className="grid__item__footer">
          <AssetControls
            user={user}
            isActive={isActive}
            selectAsset={selectAsset}
            deleteAsset={deleteAsset}
          >
            <AssetInfo info={info} timestamp={timestamp} />
          </AssetControls>
        </div>
      </div>
    );
  }

  return null;
}
