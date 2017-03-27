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

function youTubeLink(id) {
    return <div>
      <span>Video ID: {id}</span>
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

    const statusClasses = active ? "publish__label label__live label__frontpage__overlay" : "publish__label label__frontpage__novideo label__frontpage__overlay";
    const status = active ? "Live" : "Not Live";

    const selector = !active ?
      <button className="button__secondary button__active" onClick={() => selectAsset(id, version)}>
          Activate
      </button> : false;

    return <div className="grid__item">
        <div className="upload__asset__video">{embed(id, platform)}</div>
          <div className="grid__status__overlay">
            <span className={statusClasses}>{status}</span>
          </div>
        <div className="grid__item__footer">
          <span className="grid__item__title">{youTubeLink(id)}</span>
            {selector}
        </div>
    </div>;
}

function UploadAsset({ id, message, total, progress }) {
    return <div className="grid__item">
        <div className="upload__asset__video upload__asset__running">
            {progress && total ? <progress className="progress" value={progress} max={total} /> : <span className="loader"></span> }
        </div>
        <div className="grid__item__footer">
          <span className="grid__item__title">{message}</span>
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
            return <ErrorAsset  key={asset.id} message={processing.failure} />;
        } else {
            const active = asset.version === this.props.activeVersion;
            const selectable = !active && processing && (processing.status === "succeeded" || processing.status === "terminated");

            return <VideoAsset key={asset.id} active={active} selectable={selectable} selectAsset={this.props.selectAsset} {...asset} />;
        }
    };

    render() {
        const blocks = [];

        if(this.props.localUpload.total) {
            blocks.push(this.renderLocalUpload());
        }

        blocks.push(<UploadAsset key={"chris"} message={"Processing cat videos"} />);

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
            <div className="grid__list grid__list__trail">
            {content}
          </div>
          </div>
        </div>;
    }
}
