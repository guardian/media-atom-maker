import React from 'react';
import { GridImage } from '../GridImage/GridImage';
import GridImageSelect from '../utils/GridImageSelect';
import { getGridMediaId, getGridQueryParams } from '../../util/getGridMediaId';
import { isImageCropOutOfSync } from '../../util/getAspectRatio';

type Props = {
  gridDomain: string;
  video: any;
  saveAndUpdateVideo: (...args: any[]) => any;
  updateVideo: (...args: any[]) => any;
  videoEditOpen: boolean;
  updateErrors: (...args: any[]) => any;
  cropOptions: any;
};

export default class mapStateToProps extends React.Component<Props> {
  saveAndUpdateVideoImage = (image: any, location: any) => {
    const revertVideo = Object.assign({}, this.props.video);
    const newVideo = Object.assign({}, this.props.video, {
      [location]: image
    });
    this.props
      .saveAndUpdateVideo(newVideo)
      .catch(() => this.props.updateVideo(revertVideo));
  };

  getGridUrl(cropType: string) {
    const posterImage = this.props.video.posterImage;
    const queryParam = getGridQueryParams(
      cropType,
      this.props.video,
      this.props.cropOptions
    );
    if (posterImage.assets.length > 0) {
      const imageGridId = getGridMediaId(posterImage);

      if (imageGridId) {
        return `${this.props.gridDomain}/images/${imageGridId}?${queryParam}`;
      }
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

    // mediaId is in fact the media _URI_; the ID will be the last field when string is split on /
    const mediaId = this.props.video?.trailImage?.mediaId?.split('/')?.pop();

    const showImageCropWarning = isImageCropOutOfSync(this.props.video);

    return (
      <div className="video__imagebox">
        <div className="video__images">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">
              Guardian Video Thumbnail Image
            </header>
            <GridImageSelect
              image={this.props.video.posterImage}
              gridUrl={
                this.hasVerticalVideoTag()
                  ? this.getGridUrl('verticalVideo')
                  : this.getGridUrl('custom')
              }
              gridDomain={this.props.gridDomain}
              disabled={this.props.videoEditOpen}
              updateVideo={this.saveAndUpdateVideoImage}
              fieldLocation="posterImage"
            />
          </div>
          <GridImage image={this.props.video.posterImage} />
          {showImageCropWarning && (
            <div className="video__warning error">
              <p>
                Warning: The aspect ratio of the active video does not match the
                aspect ratio of this image. Please recrop the image to ensure
                the correct aspect ratios
              </p>
            </div>
          )}
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
          {/* @ts-expect-error TS(2339): Property 'pinboard-suggest-alternate-crops' does n... Remove this comment to see the full error message */}
          <pinboard-suggest-alternate-crops
            data-media-id={mediaId}
            // @ts-expect-error TS(2339): Property 'pinboard-suggest-alternate-crops' does n... Remove this comment to see the full error message
          ></pinboard-suggest-alternate-crops>
        </div>
        {this.props.video.platform !== 'Url' && (
          <div className="video__images">
            <div className="video__detailbox__header__container">
              <header className="video__detailbox__header">
                Youtube Video Thumbnail Image
              </header>
              <GridImageSelect
                image={this.props.video.youtubeOverrideImage}
                gridUrl={
                  this.hasVerticalVideoTag()
                    ? this.getGridUrl('verticalVideo')
                    : this.getGridUrl('video')
                }
                gridDomain={this.props.gridDomain}
                disabled={this.props.videoEditOpen}
                updateVideo={this.saveAndUpdateVideoImage}
                fieldLocation="youtubeOverrideImage"
              />
            </div>
            <GridImage image={this.props.video.youtubeOverrideImage} />
          </div>
        )}
      </div>
    );
  }
}
