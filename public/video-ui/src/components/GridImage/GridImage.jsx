import PropTypes from 'prop-types';
import React from 'react';
import { findAssetToUseAsThumbnail } from '../../util/imageHelpers';

export default class GridImage extends React.Component {
  static propTypes = {
    image: PropTypes.object.isRequired
  };

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
