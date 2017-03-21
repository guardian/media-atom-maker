import React from 'react';
import _ from 'lodash';
import {YouTubeEmbed, youTubeUrl} from '../utils/YouTubeEmbed';

const VIDEO_WIDTH = 320;
const VIDEO_HEIGHT = 180;

function embed(assetId, platform) {
    if(platform === "Youtube") {
        return <YouTubeEmbed id={assetId} width={VIDEO_WIDTH} height={VIDEO_HEIGHT} />;
    } else {
        return false;
    }
}

function selector(assetId, version, selectAsset, active) {
    const classes = active ? "button__active" : "button__secondary";
    const action = active ? "Active" : "Activate";

    return <button className={classes} disabled={active} onClick={() => selectAsset(assetId, version)}>
        {action}
    </button>;
}

function VideoAsset({ id, platform, version, active, selectAsset }) {
    if(!id) {
        return <div className="upload__asset">
            <div className="upload__asset__video upload__asset__empty">
                <span>No Assets Added</span>    
            </div>
        </div>;
    }

    return <div className="upload__asset">
        <div className="upload__asset__video">{embed(id, platform)}</div>
        <div className="upload__asset__caption">
            <a href={youTubeUrl(id)} title="Open on YouTube" target="_blank" rel="noopener noreferrer">
                {id}
                <span icon="open_in_new">
                    <i className="icon">open_in_new</i>
                </span>
            </a>
            {selector(id, version, selectAsset, active)}
        </div>
    </div>;
}

function UploadAsset({ message, total, progress }) {
    return <div className="upload__asset">
        <div className="upload__asset__video upload__asset__running">
            <span>{message}</span>
        </div>
        <div className="upload__asset__caption">
            {progress === undefined ? <progress /> : <progress value={progress} max={total} />}
        </div>
    </div>;
}

export default function VideoTrail({ activeVersion, assets, selectAsset, localUpload, uploads }) {
    const squares = [];

    if(localUpload.total) {
        squares.push(<UploadAsset key="upload" message="Uploading To S3" {...localUpload} />);
    }

    uploads.forEach((upload) => {
        const hidden = _.find(upload.parts, (part) => !part.uploadedToS3);

        if(!hidden) {
            squares.push(<UploadAsset key={upload.id} message="Uploading To YouTube" />);
        }
    });

    assets.forEach((asset) => {
        squares.push(<VideoAsset key={asset.id} active={asset.version === activeVersion} selectAsset={selectAsset} {...asset} />);
    });

    return <div className="upload__assets">
        {squares.length > 0 ? squares : <VideoAsset />}
    </div>;
}