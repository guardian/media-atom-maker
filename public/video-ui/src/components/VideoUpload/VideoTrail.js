import React from 'react';

const noAssets = <div className="upload__asset">
    <div className="baseline-margin upload__asset__empty">
        No Assets Added
    </div>
    <div className="upload__asset__caption">
    </div>
</div>;

export default function VideoTrail({ assets }) {
    const body = [noAssets];

    return <div className="upload__assets">
        <label>Video Trail</label>
        <div className="upload__trail">
            {body}
        </div>
    </div>;
}