import React from 'react';
import Icon from '../Icon';
import {getStore} from '../../util/storeAccessor';

const labelClasses = "publish__label label__frontpage__overlay";
const activeLabelClasses = `${labelClasses} label__live`;
const inactiveLabelClasses = `${labelClasses} label__frontpage__novideo`;

function thumbnail(assetId, platform) {
    if(platform === "Youtube") {
        const store = getStore();
        const src = store.getState().config.youtubeThumbnailUrl + assetId + "/0.jpg";
        return <img src={src} />;
    } else {
        return false;
    }
}

function link(assetId, platform) {
    if(platform == "Youtube") {
        return <a className="button" href={`https://www.youtube.com/watch?v=${assetId}`} target="_blank" rel="noopener noreferrer">
            <Icon className="icon__edit" icon="launch" />
        </a>;
    } else {
        return false;
    }
}

function selector(assetId, version, selectAsset) {
    return <a className="button" onClick={() => selectAsset(assetId, version)}>
        <Icon className="icon__edit" icon="settings_ethernet" />
    </a>;
}

function VideoAsset({ id, platform, version, active, selectAsset }) {
    if(!id) {
        return <div className="upload__asset">
            <div className="baseline-margin upload__asset__empty">No Assets Added</div>
        </div>;
    }

    return <div className="upload__asset">
        <div className="baseline-margin">{thumbnail(id, platform)}</div>
        <div className="upload__asset__caption">
            <div className="upload__asset__left">
                {active ?
                    <span className={activeLabelClasses}>Active</span> :
                    <span className={inactiveLabelClasses}>Inactive</span>
                }
            </div>
            <div className="upload__asset__right">
                {link(id, platform)}
                {selector(id, version, selectAsset)}
            </div>
        </div>
    </div>;
}

function UploadAsset({ total, progress }) {
    return <div className="upload__asset">
        <div className="baseline-margin">
            <img src="https://upload.wikimedia.org/wikipedia/en/5/52/Testcard_F.jpg" />
        </div>
        <div className="upload__asset__caption">
            <div className="upload__asset__left">
                <span className={inactiveLabelClasses}>
                    <progress value={progress} max={total} />
                </span>
            </div>
        </div>
    </div>;
}

export default function VideoTrail({ activeVersion, assets, selectAsset, upload }) {
    const squares = [];

    if(upload.total) {
        squares.push(<UploadAsset key="upload" total={upload.total} progress={upload.progress} />);
    }

    assets.forEach((asset) => {
        squares.push(<VideoAsset key={asset.id} active={asset.version === activeVersion} selectAsset={selectAsset} {...asset} />);
    });

    return <div className="upload__assets">
        <label>Video Trail</label>
        <div className="upload__trail">
            {squares.length > 0 ? squares : <VideoAsset />}
        </div>
    </div>;
}