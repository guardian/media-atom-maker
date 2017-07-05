import React from 'react';
import { VideoEmbed } from '../utils/VideoEmbed';
import { YouTubeEmbed } from '../utils/YouTubeEmbed';
import { formNames } from '../../constants/formNames';
import VideoPoster from '../VideoPoster/VideoPoster';
import GridImageSelect from '../utils/GridImageSelect';

export default class VideoPreview extends React.Component {

  saveAndUpdateVideoPoster = poster => {
    const newVideo = Object.assign({}, this.props.video, {
      posterImage: poster
    });
    this.props.saveAndUpdateVideo(newVideo);
  };

  renderPreview() {
    const activeVersion = this.props.video.activeVersion;
    const assets = this.props.video.assets || [];
    const active = assets.filter(asset => asset.version === activeVersion);

    if (active.length === 0) {
      return <div className="baseline-margin">No Active Video</div>;
    }

    if (active.length === 1 && active[0].platform === 'Youtube') {
      return <YouTubeEmbed id={active[0].id} className="baseline-margin" />;
    }

    const sources = active.map(asset => {
      return { src: asset.id, mimeType: asset.mimeType };
    });

    return <VideoEmbed sources={sources} />;
  }

  render() {
    return (
      <div className="video-preview">
        {this.renderPreview()}
        <div className="video__detailbox">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">
              Poster Image
            </header>
            <GridImageSelect
              updateVideo={this.saveAndUpdateVideoPoster}
              gridUrl={this.props.config.gridUrl}
              disabled={this.props.videoEditOpen}
              createMode={false}
            />
          </div>
          <VideoPoster
            video={this.props.video || {}}
            updateVideo={this.props.saveAndUpdateVideo}
            formName={formNames.posterImage}
            updateErrors={this.props.updateErrors}
          />
        </div>
      </div>
    );
  }
}
