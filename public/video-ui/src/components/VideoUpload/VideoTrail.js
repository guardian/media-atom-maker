import React from 'react';
import {YouTubeEmbed, youTubeUrl} from '../utils/YouTubeEmbed';
import Icon from '../Icon';
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
    if(!selectAsset) {
        return false;
    }

    const classes = active ? "button__active" : "button__secondary";
    const action = active ? "Active" : "Activate";

    return ;
    // <button className={classes} disabled={active} onClick={() => selectAsset(assetId, version)}>
    //     {action}
    // </button>;
}

function youTubeLink(id) {
    return <div className="grid__item__footer">
      <span className="grid__item__title grid__item__title__assets">Video ID: {id}</span>
      <a href={youTubeUrl(id)}><Icon icon="open_in_new" className="icon__assets"></Icon></a>
      </div>;
}

function ErrorAsset({ message }) {
    return <div className="grid__item">
        <div className="upload__asset__video upload__asset__empty">
            <span>{message}</span>
        </div>
    </div>;
}

function VideoAsset({ id, platform, version, active, selectAsset }) {
    return <div className="grid__item">
        <div className="upload__asset__video">{embed(id, platform, active)}</div>
          <div className="grid__status__overlay">
            <span className="publish__label label__live label__frontpage__overlay">{id}</span>
          </div>
        <div className="grid__item__footer">
            {youTubeLink(id)}
            {selector(id, version, selectAsset, active)}
        </div>
    </div>;
}

function UploadAsset({ id, message, total, progress }) {
    return <div className="grid__item">
        <div className="upload__asset__video upload__asset__running">
            <span className="loader"></span>
            {progress && total ? <progress className="progress" value={progress} max={total} /> : false }
        </div>
        <div className="grid__item__footer">
          <span className="grid__item__title">{message}</span>
            {id ? youTubeLink(id) : false}
        </div>
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

    pollIfRequired = () => {
        if(this.props.uploads.length > 0) {
            this.props.getUploads();
            this.props.getVideo();
        }

        if(_.some(this.state.status, (entry) => entry.status === "processing")) {
            this.enrichWithStatus(this.props.assets);
        }
    };

    enrichWithStatus = (assets) => {
        if(assets.length === 0) {
            return;
        }

        const ids = assets.map((asset) => asset.id);
        getProcessingStatus(ids).then((resp) => {
            this.setState({ status: resp });
        });
    };

    renderLocalUpload = () => {
        // Multiply by 2 to give the impression of a continuous progress bar once this component is swapped out with the remote upload one
        const total = this.props.localUpload.total * 2;
        const progress = this.props.localUpload.progress;

        return <UploadAsset key="localUpload" message="Uploading To S3" total={total} progress={progress} />;
    };

    renderRemoteUpload = (id, total, progress) => {
        // See renderLocalUpload() for why we multiply by 2
        return <UploadAsset key={id} message="Uploading to YouTube" total={total * 2} progress={progress} />;
    };

    renderAsset = (asset) => {
        const processing = _.find(this.state.status, (status) => status.id === asset.id);

        if(processing && processing.status === "processing") {
            const message = processing.timeLeftMs === 0 ?
                "YouTube Processing" :
                `YouTube Processing (${processing.timeLeftMs / 1000}s left)`;

            return <UploadAsset key={asset.id} id={asset.id} message={message} />;
        } else if(processing && processing.status === "failed") {
            return <ErrorAsset key={asset.id} message={processing.failure} />;
        } else {
            const active = asset.version === this.props.activeVersion;

            let selectAsset = null;
            if(processing && (processing.status === "succeeded" || processing.status === "terminated")) {
                selectAsset = this.props.selectAsset;
            }

            return <VideoAsset key={asset.id} active={active} selectAsset={selectAsset} {...asset} />;
        }
    };

    render() {
        const blocks = [];

        blocks.push(<UploadAsset key={"chris"} message="Uploading to YouTube" total={10} progress={3} />);

        if(this.props.localUpload.total) {
            blocks.push(this.renderLocalUpload());
        }

        this.props.uploads.forEach((upload) => {
            const total = upload.parts[upload.parts.length - 1].end;
            const progress = upload.progress.uploadedToS3 + upload.progress.uploadedToYouTube;

            // Don't show other users uploads until they have reached YouTube
            if(upload.progress.uploadedToS3 === total) {
                blocks.push(this.renderRemoteUpload(upload.id, total, progress));
            }
        });

        blocks.push(...this.props.assets.map(this.renderAsset));

        const content = blocks.length > 0 ? blocks : <ErrorAsset message="No Assets Uploaded" />;

      return <div className="video__detail__page__trail">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">Video trail</header>
          </div>
          <div className="grid">
            <div className="grid__list">
            {content}
          </div>
          </div>
        </div>;
    }
}
