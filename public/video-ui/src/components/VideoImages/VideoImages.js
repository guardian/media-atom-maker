import React from 'react';
import PropTypes from 'prop-types';
import { formNames } from '../../constants/formNames';
import GridImage from '../GridImage/GridImage';
import GridImageSelect from '../utils/GridImageSelect';

export default class VideoImages extends React.Component {
  static propTypes = {
    config: PropTypes.object.isRequired,
    video: PropTypes.object.isRequired,
    saveAndUpdateVideo: PropTypes.func.isRequired,
    videoEditOpen: PropTypes.bool.isRequired,
    updateErrors: PropTypes.func.isRequired
  };

  saveAndUpdateVideoPoster = (poster, location) => {
    const newVideo = Object.assign({}, this.props.video, {
      [location]: poster
    });
    this.props.saveAndUpdateVideo(newVideo);
  };

  render() {
    return (
      <div className="video__imagebox">
        <div className="video__detailbox">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">
              Main Image (YouTube poster)
            </header>
            <GridImageSelect
              updateVideo={this.saveAndUpdateVideoPoster}
              gridUrl={this.props.config.gridUrl}
              disabled={this.props.videoEditOpen}
              fieldLocation="posterImage"
            />
          </div>
          <GridImage image={this.props.video.posterImage} />
        </div>
        <div className="video__detailbox">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">
              Composer Trail Image
            </header>
            <GridImageSelect
              updateVideo={this.saveAndUpdateVideoPoster}
              gridUrl={this.props.config.gridUrl}
              disabled={this.props.videoEditOpen || !this.props.video.posterImage}
              isComposerImage={true}
              posterImage={this.props.video.posterImage}
              fieldLocation="trailImage"
            />
          </div>
          <GridImage image={this.props.video.trailImage} />
        </div>
      </div>
    );
  }
}
