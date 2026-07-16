import React from 'react';
import { findAssetToUseAsThumbnail } from '../../util/imageHelpers';
import type { Image } from '../../services/VideosApi';

type Props = {
  image: Image;
};

export const GridImage = ({ image }: Props) => {
  const maybeImageAsset = image
    ? findAssetToUseAsThumbnail(image)
    : undefined;

  const renderImage = () => {
    if (maybeImageAsset && maybeImageAsset.file) {
      return (
        <div className="form__image">
          <img src={maybeImageAsset.file} />
        </div>
      );
    }

    return <div>no image</div>;
  };

  return (
    <div className="form__row">
      <div className="form__imageselect">{renderImage()}</div>
    </div>
  );
};
