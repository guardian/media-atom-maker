import React from 'react';
import Icon from '../Icon';
import {getStore} from '../../util/storeAccessor';

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

function VideoAsset({ id, platform, active }) {
    if(!id) {
        return <div className="upload__asset">
            <div className="baseline-margin upload__asset__empty">No Assets Added</div>
        </div>;
    }

    return <div className="upload__asset">
        <div className="baseline-margin">{thumbnail(id, platform)}</div>
        <div className="upload__asset__caption">
            {active ?
                <span className="publish__label label__live label__frontpage__overlay">Active</span> :
                <span className="publish__label label__frontpage__novideo label__frontpage__overlay">Inactive</span>
            }
            {link(id, platform)}
        </div>
    </div>;
}

export default function VideoTrail({ activeVersion, assets }) {
    const body = assets.map((asset) => {
        return <VideoAsset key={asset.id} active={asset.version === activeVersion} {...asset} />;
    });

    return <div className="upload__assets">
        <label>Video Trail</label>
        <div className="upload__trail">
            {body.length > 0 ? body : <VideoAsset />}
        </div>
    </div>;
}