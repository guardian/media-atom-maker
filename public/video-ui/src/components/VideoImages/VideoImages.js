import React from 'react';
import PropTypes from 'prop-types';
import GridImage from '../GridImage/GridImage';
import GridImageSelect from '../utils/GridImageSelect';
import { getGridMediaId } from '../../util/getGridMediaId';

export default class VideoImages extends React.Component {
  static propTypes = {
    gridDomain: PropTypes.string.isRequired,
    video: PropTypes.object.isRequired,
    saveAndUpdateVideo: PropTypes.func.isRequired,
    videoEditOpen: PropTypes.bool.isRequired,
    updateErrors: PropTypes.func.isRequired
  };

  saveAndUpdateVideoImage = (image, location) => {
    const newVideo = Object.assign({}, this.props.video, {
      [location]: image
    });
    this.props.saveAndUpdateVideo(newVideo);
  };

  getGridUrl(cropType) {
    const posterImage = this.props.video.posterImage;

    const queryParam = cropType == "verticalVideo" ?
      `cropType=${cropType}&customRatio=${cropType},9,16` :
      `cropType=${cropType}`;

    if (posterImage.assets.length > 0) {
      const imageGridId = getGridMediaId(posterImage);

      return `${this.props.gridDomain}/images/${imageGridId}?${queryParam}`;
    }

    return `${this.props.gridDomain}?${queryParam}`;
  }

  hasVerticalVideoTag() {
    const tags = this.props.video.keywords || [];
    return tags.includes('tone/vertical-video');
  }

  render() {
    const trailImageDisabled =
      this.props.videoEditOpen ||
      this.props.video.posterImage.assets.length === 0;

    return (
      <div className="video__imagebox">
        <div className="video__images">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">
              Guardian Video Thumbnail Image
            </header>
            <GridImageSelect
              image={this.props.video.posterImage}
              gridUrl={this.hasVerticalVideoTag() ? this.getGridUrl('verticalVideo'): this.getGridUrl('video')}
              gridDomain={this.props.gridDomain}
              disabled={this.props.videoEditOpen}
              updateVideo={this.saveAndUpdateVideoImage}
              fieldLocation="posterImage"
            />
          </div>
          <GridImage image={this.props.video.posterImage} />
        </div>
        <div className="video__images">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">
              Composer Trail Image
            </header>
            <GridImageSelect
              image={this.props.video.trailImage}
              gridUrl={this.getGridUrl('landscape')}
              gridDomain={this.props.gridDomain}
              disabled={trailImageDisabled}
              updateVideo={this.saveAndUpdateVideoImage}
              fieldLocation="trailImage"
            />
          </div>
          <GridImage image={this.props.video.trailImage} />
        </div>
        <div className="video__images">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">
              Youtube Video Thumbnail Image
            </header>
            <GridImageSelect
              image={this.props.video.youtubeOverrideImage}
              gridUrl={this.hasVerticalVideoTag() ? this.getGridUrl('verticalVideo'): this.getGridUrl('video')}
              gridDomain={this.props.gridDomain}
              disabled={this.props.videoEditOpen}
              updateVideo={this.saveAndUpdateVideoImage}
              fieldLocation="youtubeOverrideImage"
            />
          </div>
          <GridImage image={this.props.video.youtubeOverrideImage} />
        </div>
      </div>
    );
  }
}
