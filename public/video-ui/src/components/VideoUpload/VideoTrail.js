import React from 'react';
import {YouTubeEmbed, youTubeUrl} from '../utils/YouTubeEmbed';
import {GuardianLogo} from '../Icon';
import {getProcessingStatus} from '../../services/YoutubeApi';
import _ from 'lodash';

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
            <GuardianLogo />
            <span className="upload__asset__message">{message}</span>
        </div>
        <div className="upload__asset__caption">
            {progress === undefined ? <progress /> : <progress value={progress} max={total} />}
        </div>
    </div>;
}

function buildUpload(upload) {
    const total = upload.parts[upload.parts.length - 1].end;
    const progress = upload.progress.uploadedToS3 + upload.progress.uploadedToYouTube;
    
    if(upload.progress.uploadedToS3 === total) {
        return <UploadAsset key={upload.id} message="Uploading to YouTube" total={total * 2} progress={progress} />;
    } else {
        return false; // don't show uploads until they have reached YouTube
    }
}

function VideoSquares({ activeVersion, assets, selectAsset, localUpload, uploads }) {
    const squares = [];

    if(localUpload.total) {
        squares.push(<UploadAsset key="upload" message="Uploading To S3" total={localUpload.total * 2} progress={localUpload.progress }/>);
    }

    uploads.forEach((upload) => {
        const element = buildUpload(upload);

        if(element) {
            squares.push(element);
        }
    });

    assets.forEach((asset) => {
        squares.push(<VideoAsset key={asset.id} active={asset.version === activeVersion} selectAsset={selectAsset} {...asset} />);
    });

    return <div className="upload__assets">
        {squares.length > 0 ? squares : <VideoAsset />}
    </div>;
}

export default class VideoTrail extends React.Component {
    polling = null;
    state = { status: [] };

    constructor(props) {
        super(props);
        
        this.polling = setInterval(() => this.pollIfRequired(), 5000);
        this.enrichWithStatus(props.assets);
    }

    componentDidUpdate(prevProps) {
        if(!_.isEqual(prevProps.assets, this.props.assets)) {
            this.enrichWithStatus(this.props.assets);
        }
    }

    componentWillUnmount() {
        clearInterval(this.polling);
    }

    pollIfRequired() {
        if(this.props.uploads.length > 0) {
            this.props.getUploads();
            this.props.getVideo();
        }

        if(_.some(this.state.status, (entry) => entry.status === "processing")) {
            this.enrichWithStatus(this.props.assets);
        }
    }

    enrichWithStatus(assets) {
        if(assets.length === 0) {
            return;
        }

        const ids = assets.map((asset) => asset.id);
        getProcessingStatus(ids).then((resp) => {
            this.setState({ status: resp });
        }); 
    }

    render() {
        return <VideoSquares {...this.props} />;
    }
}