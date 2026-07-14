import React from 'react';
import { findAssetToUseAsThumbnail } from '../../util/imageHelpers';
import type { Image } from '../../services/VideosApi';

type Props = {
  image: Image;
};

export default class GridImage extends React.Component<Props> {
  renderImage() {
    const maybeImageAsset = this.props.image
      ? findAssetToUseAsThumbnail(this.props.image)
      : undefined;

    if (maybeImageAsset && maybeImageAsset.file) {
      return (
        <div className="form__image">
          <img src={maybeImageAsset.file} />
        </div>
      );
    }

    return <div>no image</div>;
  }

  render() {
    return (
      <div className="form__row">
        <div className="form__imageselect">{this.renderImage()}</div>
      </div>
    );
  }
}
