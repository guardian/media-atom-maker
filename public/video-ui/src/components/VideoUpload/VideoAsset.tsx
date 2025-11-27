import moment from 'moment';
import React, { ChangeEventHandler, ReactNode } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  ClientAsset,
  deleteSubtitle,
  startSubtitleFileUpload,
} from '../../slices/s3Upload';
import { selectVideo } from '../../slices/video';
import { AppDispatch } from '../../util/setupStore';
import DeleteButton from '../DeleteButton';
import Icon, { SubtitlesIcon } from '../Icon';
import { VideoEmbed } from '../utils/VideoEmbed';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import type {VideoPlayerFormat} from "../../constants/videoCreateOptions";

function presenceInitials(email: string) {
  if (!email) return;
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
  assetId,
  user,
  children,
  isActive,
  selectAsset,
  deleteAsset,
  activatingAssetNumber,
  isActivating,
  isUploadInProgress
}: {
  assetId: string;
  user: string;
  isActive?: boolean;
  selectAsset: { (): void };
  deleteAsset: { (): void };
  children: ReactNode;
  activatingAssetNumber: number;
  isActivating: boolean;
  isUploadInProgress?: boolean;
}) {
  const className = isActivating ? 'btn btn--loading' : 'btn';

  const video = useSelector(selectVideo);

  const cannotActivateAsset =
    typeof activatingAssetNumber === 'number' || isUploadInProgress;

  const cannotDeleteAsset =
    cannotActivateAsset ||
    !video.assets.some(videoAsset => videoAsset.version.toString() === assetId);

  const userCircle = (
    <div className="video-trail__presence_indicator" title={user}>
      {presenceInitials(user)}
    </div>
  );

  const activateButton = (
    <button
      className={className}
      style={{ paddingRight: 20 }}
      disabled={cannotActivateAsset}
      onClick={selectAsset}
    >
      Activate
    </button>
  );

  const deleteButton = (
    <DeleteButton
      tooltip="Remove asset from Atom (does not affect YouTube.com)"
      tooltipWhenDisabled="Cannot delete while upload is still being processed."
      disabled={cannotDeleteAsset}
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

function AssetInfo({
  info,
  timestamp
}: {
  info: string;
  timestamp?: number | false;
}) {
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
  id?: string | number;
  isActive: boolean;
  sources: unknown[];
}) {
  const embed = id ? (
    <YouTubeEmbed id={id} largePreview={true} className={undefined} />
  ) : (
    <VideoEmbed sources={sources} posterUrl={undefined} />
  );

  return (
    <div className={'video-trail__asset'}>
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
        <div className="video-trail__status__overlay">
          <span className="publish__label label__live label__frontpage__overlay">
            Active
          </span>
        </div>
      )}
    </div>
  );
}

function AssetProgress({ failed, current, total }: ClientAsset['processing']) {
  if (failed) {
    return (
      <div>
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

function SubtitleDetails({ filename }: { filename: string }) {
  if (!filename) {
    return <div className="subtitle__title">No Subtitle File Attached</div>;
  }

  return (
    <div className="subtitle__details">
      <div className="subtitle__title">{filename}</div>
    </div>
  );
}
function SubtitleActions({
  filename,
  onUpload,
  onDelete
}: {
  filename: string;
  onUpload: { (file: File): void };
  onDelete: { (): void };
}) {
  const fileInputRef = React.useRef();

  const handleFileChange: ChangeEventHandler<HTMLInputElement> = event => {
    const input = event.target;
    const file = event.target.files[0];

    if (!file) return;
    const isSRT =
      file.name.endsWith('.srt') || file.type === 'application/x-subrip';

    if (!isSRT) {
      alert('Invalid file type. Please upload a .srt subtitle file.');
      input.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = function (e) {
      const text = e.target.result as string;

      const isSRTFormat =
        /\d{2}:\d{2}:\d{2},\d{3} --> \d{2}:\d{2}:\d{2},\d{3}/.test(text);

      if (isSRT && !isSRTFormat) {
        alert('The file content does not match the expected subtitle format.');
        input.value = '';
        return;
      }

      onUpload(file);
      input.value = '';
    };

    reader.readAsText(file);
  };

  const handleClick = () => {
    (fileInputRef.current as HTMLInputElement)?.click();
  };

  return (
    <div className="subtitle__actions">
      <input
        type="file"
        ref={fileInputRef}
        accept=".srt"
        style={{ display: 'none' }}
        onChange={handleFileChange}
      />
      <button className="btn upload__activate-btn" onClick={handleClick}>
        <Icon icon="backup">{filename ? 'Replace' : 'Upload'}</Icon>
      </button>
      {filename && (
        <button className="subtitle__delete-btn" onClick={onDelete}>
          Delete Subtitle File
        </button>
      )}
    </div>
  );
}

export function Asset({
  videoId,
  upload,
  isActive,
  selectAsset,
  deleteAsset,
  activatingAssetNumber,
  videoPlayerFormat
}: {
  videoId: string;
  upload: ClientAsset;
  isActive: boolean;
  selectAsset: { (): void };
  deleteAsset: { (): void };
  activatingAssetNumber: number;
  videoPlayerFormat?: VideoPlayerFormat
}) {
  const dispatch = useDispatch<AppDispatch>();

  const { asset, metadata, processing } = upload;

  const user = metadata?.user ?? '';
  const info = `Asset ${upload.id} - ${metadata?.originalFilename || '(no filename)'}`;
  const timestamp = metadata?.startTimestamp || false;

  const isSelfHosted = asset && asset.sources;
  const subtitleFilename = metadata?.subtitleFilename;

  /**
   * We support subtitles on Loops, but not Cinemagraphs (designed to be silent and decorative), and Standard videos
   * (which use the browser player).
   *
   * We can't simply check for videoPlayerFormat === 'Loop' here as videoPlayerFormat is undefined on videos
   * created before videoPlayerFormat was introduced.
  **/
  const videoSupportsSubtitles = isSelfHosted && (videoPlayerFormat === 'Loop' || videoPlayerFormat === undefined);

  const subtitlePanel = videoSupportsSubtitles && (
    <div className="video-trail__item__subtitles">
      <div className="subtitle__container">
        <SubtitlesIcon />
        <SubtitleDetails filename={subtitleFilename} />
      </div>
      <SubtitleActions
        filename={subtitleFilename}
        onUpload={file =>
          dispatch(
            startSubtitleFileUpload({ file, id: videoId, version: upload.id })
          )
        }
        onDelete={() =>
          dispatch(deleteSubtitle({ id: videoId, version: upload.id }))
        }
      />
    </div>
  );

  if (processing && asset) {
    // reprocessing subtitle
    return (
      <div className="video-trail__item">
        <div className="video-trail__upload">
          <div>{processing.status}</div>
          <AssetProgress {...processing} />
        </div>
        <div className="video-trail__item__details">
          <AssetControls
            assetId={upload.id}
            user={user}
            isActive={isActive}
            selectAsset={selectAsset}
            deleteAsset={deleteAsset}
            isUploadInProgress={false}
            isActivating={activatingAssetNumber === Number(upload.id)}
            activatingAssetNumber={activatingAssetNumber}
          >
            <AssetInfo info={info} timestamp={timestamp} />
          </AssetControls>
        </div>
        {subtitlePanel}
      </div>
    );
  }

  if (processing) {
    return (
      <div className="video-trail__item">
        <div className="video-trail__upload">
          <AssetProgress {...processing} />
          <div>{processing.status}</div>
        </div>
        <div className="video-trail__item__details">
          <AssetControls
            assetId={upload.id}
            user={user}
            selectAsset={selectAsset}
            deleteAsset={deleteAsset}
            isUploadInProgress={true}
            isActivating={activatingAssetNumber === Number(upload.id)}
            activatingAssetNumber={activatingAssetNumber}
          >
            <AssetInfo info={processing.status} />
          </AssetControls>
        </div>
      </div>
    );
  }

  if (asset) {
    return (
      <div className="video-trail__item">
        <AssetDisplay
          isActive={isActive}
          id={asset.id}
          sources={asset.sources}
        />
        <div className="video-trail__item__details">
          <AssetControls
            assetId={upload.id}
            user={user}
            isActive={isActive}
            selectAsset={selectAsset}
            deleteAsset={deleteAsset}
            isActivating={activatingAssetNumber === Number(upload.id)}
            activatingAssetNumber={activatingAssetNumber}
          >
            <AssetInfo info={info} timestamp={timestamp} />
          </AssetControls>
        </div>
        {subtitlePanel}
      </div>
    );
  }

  return null;
}
