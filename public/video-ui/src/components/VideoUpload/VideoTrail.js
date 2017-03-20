import React from 'react';

const labelClasses = "publish__label label__frontpage__overlay";
const activeLabelClasses = `${labelClasses} label__live`;
const inactiveLabelClasses = `${labelClasses} label__frontpage__novideo`;

const VIDEO_WIDTH = 320;
const VIDEO_HEIGHT = 180;

function embed(assetId, platform) {
    if(platform === "Youtube") {
        const props = {
            type: "text/html",
            width: VIDEO_WIDTH, 
            height: VIDEO_HEIGHT,
            src: `https://www.youtube.com/embed/${assetId}?showinfo=0`,
            frameBorder: 0
        };

        return <iframe {...props}></iframe>;
    } else {
        return false;
    }
}

function selector(assetId, version, selectAsset) {
    return <button className="button__secondary" onClick={() => selectAsset(assetId, version)}>
        Select
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
            {active ?
                <div className={activeLabelClasses}>Active</div> :
                <div className={inactiveLabelClasses}>Inactive</div>
            }
            {selector(id, version, selectAsset)}
        </div>
    </div>;
}

function UploadAsset({ total, progress }) {
    return <div className="upload__asset">
        <div className="upload__asset__video">
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
        {squares.length > 0 ? squares : <VideoAsset />}
    </div>;
}