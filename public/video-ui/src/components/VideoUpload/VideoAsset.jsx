import React from 'react';
import moment from 'moment';
import Icon, {SubtitlesIcon} from '../Icon';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import { VideoEmbed } from '../utils/VideoEmbed';
import DeleteButton from '../DeleteButton';

function presenceInitials(email) {
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

function AssetControls({ user, children, isActive, selectAsset, deleteAsset }) {
    const userCircle =
        <div className="video-trail__presence_indicator" title={user}>
                {presenceInitials(user)}
        </div>

  const activateButton =
    <button className="btn upload__activate-btn" onClick={selectAsset}>
      Activate
    </button>


  const deleteButton = <DeleteButton
    tooltip="Remove asset from Atom (does not affect YouTube.com)"
    onDelete={deleteAsset}
  />

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

function AssetInfo({ info, timestamp }) {
  const dateTime = timestamp && moment(timestamp).format('YYYY/MM/DD HH:mm:ss')

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

function AssetDisplay({id, isActive, sources}) {
  const embed = id ? <YouTubeEmbed id={id} largePreview={true}/> : <VideoEmbed sources={sources}/>

  return (
    <div className={"video-trail__upload"}>
      {embed}
      {id &&
        <a className={'upload__link'}
           href={`https://www.youtube.com/watch?v=${id}`}
           target={'_blank'}
           rel={'noopener noreferrer'}>
          <Icon icon="open_in_new" className="icon__assets"/>
        </a>}
      {isActive &&
        <div className="video-trail__status__overlay">
            <span className="publish__label label__live label__frontpage__overlay">
              Active
            </span>
        </div>
      }
    </div>
  );
}

function AssetProgress({ failed, current, total }) {
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
    return <progress className="progress" value={current} max={total} />
  }
  return <span className="loader" />;
}

function SubtitleDetails({ subtitles }) {
    if (!subtitles) {
        return <div className="subtitle__title">No Subtitle File Attached</div>;
    }

    const { title, timestamp } = subtitles;
    const dateTime = moment(timestamp).format('YYYY/MM/DD HH:mm:ss')

    return (
        <div className="subtitle__details">
            <div className="subtitle__title">{title}</div>
            <div className="subtitle__datetime">{dateTime}</div>
        </div>
    );
}
function SubtitleActions({ subtitles, onUpload, onDelete}) {
  const fileInputRef = React.useRef();

  const handleFileChange = (event) => {
    const file = event.target.files[0];

    if (!file) return;
    const isSRT = file.name.endsWith('.srt') || file.type === 'application/x-subrip';
    const isVTT = file.name.endsWith('.vtt') || file.type === 'text/vtt';


    if (!isSRT && !isVTT) {
      alert('Invalid file type. Please upload a .srt or .vtt subtitle file.');
      return;
    }

    const reader = new FileReader();
    reader.onload = function(e) {
      const text = e.target.result;

      const isSRTFormat = /\d{2}:\d{2}:\d{2},\d{3} --> \d{2}:\d{2}:\d{2},\d{3}/.test(text);
      const isVTTFormat = /^WEBVTT/m.test(text) &&
        /\d{2}:\d{2}:\d{2}\.\d{3} --> \d{2}:\d{2}:\d{2}\.\d{3}/.test(text);

      if ((isSRT && !isSRTFormat) || (isVTT && !isVTTFormat)) {
        alert('The file content does not match the expected subtitle format.');
        return;
      }

      onUpload(file);
    };
  };

    const handleClick = () => {
      fileInputRef.current?.click();
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
          <Icon icon="backup">
            {subtitles ? "Replace" : "Upload"}
          </Icon>
        </button>
        {subtitles && <button className="subtitle__delete-btn" onClick={onDelete}>Delete Subtitle File</button>}
      </div>
    );
}

export function Asset({upload, isActive, selectAsset, deleteAsset, startSubtitleFileUpload, deleteSubtitle,permissions}) {

  const { asset, metadata, processing } = upload;

  const user =  metadata?.user ?? "";
  const info = metadata?.originalFilename || `Version ${upload.id}`;
  const timestamp =  metadata?.startTimestamp || false;

  const subtitles = asset?.sources?.find(source => source.mimeType === "WEBVTT")

  if (processing) {
    return (
      <div className="video-trail__item">
        <div className="upload">
          <AssetProgress {...processing} />
        </div>
        <div className="video-trail__item__details">
          <AssetControls user={user} selectAsset={selectAsset} deleteAsset={deleteAsset}>
            <AssetInfo info={processing.status} />
          </AssetControls>
        </div>
      </div>
    );
  }

  if (asset) {
    return (
      <div className="video-trail__item">
        <AssetDisplay isActive={isActive} id={asset.id} sources={asset.sources} />
        <div className="video-trail__item__details">
          <AssetControls user={user} isActive={isActive} selectAsset={selectAsset} deleteAsset={deleteAsset}>
            <AssetInfo info={info} timestamp={timestamp} />
          </AssetControls>

        </div>


        { permissions?.addSubtitles &&

          <div className="video-trail__item__subtitles">
              <div className="subtitle__container">
                  <SubtitlesIcon />
                  <SubtitleDetails subtitles={subtitles} />
              </div>
              <SubtitleActions subtitles={subtitles} onUpload={(file) =>  startSubtitleFileUpload({ file, id: asset.sources[0].src, version: upload.id })} onDelete={deleteSubtitle} />
          </div>
        }
      </div>
    );
  }

  return null;
}
