import React from 'react';
import { Video } from '../../services/VideosApi';
import { getGridMediaId } from '../../util/getGridMediaId';
import GridImage from '../GridImage/GridImage';
import GridImageSelect from '../utils/GridImageSelect';

type Props = {
  gridDomain: string;
  video: Video;
  saveAndUpdateVideo: (video: Video) => Promise<void>;
  updateVideo: (video: Video) => void;
  videoEditOpen: boolean;
};

export type ImageField = 'posterImage' | 'trailImage' | 'youtubeOverrideImage';

export default function VideoImages({
  gridDomain,
  video,
  saveAndUpdateVideo,
  updateVideo,
  videoEditOpen
}: Props) {
  const saveAndUpdateVideoImage = (image: any, location: ImageField) => {
    const revertVideo = Object.assign({}, video);
    const newVideo = Object.assign({}, video, {
      [location]: image
    });
    saveAndUpdateVideo(newVideo).catch(() => updateVideo(revertVideo));
  };

  const getGridUrl = (cropType: string) => {
    const posterImage = video.posterImage;

    const queryParam =
      cropType == 'verticalVideo'
        ? `cropType=${cropType}&customRatio=${cropType},9,16`
        : `cropType=${cropType}`;

    if (posterImage?.assets.length && posterImage.assets.length > 0) {
      const imageGridId = getGridMediaId(posterImage);

      if (imageGridId) {
        return `${gridDomain}/images/${imageGridId}?${queryParam}`;
      }
    }

    return `${gridDomain}?${queryParam}`;
  };

  const hasVerticalVideoTag = () => {
    const tags = video.keywords || [];
    return tags.includes('tone/vertical-video');
  };

  const trailImageDisabled =
    videoEditOpen || video.posterImage?.assets.length === 0;

  // mediaId is in fact the media _URI_; the ID will be the last field when string is split on /
  const mediaId = video?.trailImage?.mediaId?.split('/')?.pop();

  return (
    <div className="video__imagebox">
      <div className="video__images">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">
            Guardian Video Thumbnail Image
          </header>
          <GridImageSelect
            image={video.posterImage}
            gridUrl={
              hasVerticalVideoTag()
                ? getGridUrl('verticalVideo')
                : getGridUrl('video')
            }
            gridDomain={gridDomain}
            disabled={videoEditOpen}
            updateVideo={saveAndUpdateVideoImage}
            fieldLocation="posterImage"
          />
        </div>
        <GridImage image={video.posterImage} />
      </div>
      <div className="video__images">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">
            Composer Trail Image
          </header>
          <GridImageSelect
            image={video.trailImage}
            gridUrl={getGridUrl('landscape')}
            gridDomain={gridDomain}
            disabled={trailImageDisabled}
            updateVideo={saveAndUpdateVideoImage}
            fieldLocation="trailImage"
          />
        </div>
        <GridImage image={video.trailImage} />
        <pinboard-suggest-alternate-crops
          data-media-id={mediaId}
        ></pinboard-suggest-alternate-crops>
      </div>
      {video.platform !== 'Url' && (
        <div className="video__images">
          <div className="video__detailbox__header__container">
            <header className="video__detailbox__header">
              Youtube Video Thumbnail Image
            </header>
            <GridImageSelect
              image={video.youtubeOverrideImage}
              gridUrl={
                hasVerticalVideoTag()
                  ? getGridUrl('verticalVideo')
                  : getGridUrl('video')
              }
              gridDomain={gridDomain}
              disabled={videoEditOpen}
              updateVideo={saveAndUpdateVideoImage}
              fieldLocation="youtubeOverrideImage"
            />
          </div>
          <GridImage image={video.youtubeOverrideImage} />
        </div>
      )}
    </div>
  );
}
