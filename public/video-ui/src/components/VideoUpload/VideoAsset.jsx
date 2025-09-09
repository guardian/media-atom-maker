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
    <div className={"video-trail__asset"}>
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
      <div>
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

function SubtitleDetails({ filename }) {
    if (!filename) {
        return <div className="subtitle__title">No Subtitle File Attached</div>;
    }

    return (
        <div className="subtitle__details">
            <div className="subtitle__title">{filename}</div>
        </div>
    );
}
function SubtitleActions({ filename, onUpload, onDelete}) {
  const fileInputRef = React.useRef();

  const handleFileChange = (event) => {
    const input = event.target;
    const file = event.target.files[0];

    if (!file) return;
    const isSRT = file.name.endsWith('.srt') || file.type === 'application/x-subrip';
    const isVTT = file.name.endsWith('.vtt') || file.type === 'text/vtt';


    if (!isSRT && !isVTT) {
      alert('Invalid file type. Please upload a .srt or .vtt subtitle file.');
      input.value = '';
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
        input.value = '';
        return;
      }

      onUpload(file);
      input.value = '';
    };

    reader.readAsText(file);
  };

    const handleClick = () => {
      fileInputRef.current?.click();
    };

    return (
      <div className="subtitle__actions">
        <input
          type="file"
          ref={fileInputRef}
          accept=".srt,.vtt"
          style={{ display: 'none' }}
          onChange={handleFileChange}
        />
        <button className="btn upload__activate-btn" onClick={handleClick}>
          <Icon icon="backup">
            {filename ? "Replace" : "Upload"}
          </Icon>
        </button>
        {filename && <button className="subtitle__delete-btn" onClick={onDelete}>Delete Subtitle File</button>}
      </div>
    );
}

export function Asset({videoId, upload, isActive, selectAsset, deleteAsset, startSubtitleFileUpload, deleteSubtitle, permissions}) {

  const { asset, metadata, processing } = upload;

  const user =  metadata?.user ?? "";
  const info = `Asset ${upload.id} - ${metadata?.originalFilename || '(no filename)'}`;
  const timestamp =  metadata?.startTimestamp || false;

  const isSelfHosted = asset && asset.sources;
  const subtitleFilename = metadata?.subtitleFilename;

  const subtitlePanel = isSelfHosted && permissions?.addSubtitles &&
    <div className="video-trail__item__subtitles">
      <div className="subtitle__container">
        <SubtitlesIcon />
        <SubtitleDetails filename={subtitleFilename} />
      </div>
      <SubtitleActions filename={subtitleFilename} onUpload={(file) => startSubtitleFileUpload({ file, id: videoId, version: upload.id })} onDelete={() => deleteSubtitle({ id: videoId, version: upload.id })} />
    </div>;

  if (processing && asset) {
    return (
      <div className="video-trail__item">
        <div className="video-trail__upload">
          <AssetProgress {...processing} />
          <div>{processing.status}</div>
        </div>
        <div className="video-trail__item__details">
          <AssetControls user={user} selectAsset={selectAsset} deleteAsset={deleteAsset}>
            <AssetInfo info={info} timestamp={timestamp} />
          </AssetControls>
        </div>
        { subtitlePanel }
      </div>
    );
  }

  if (processing) {
    return (
      <div className="video-trail__item">
        <div className="video-trail__upload">
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
        { subtitlePanel }
      </div>
    );
  }

  return null;
}
