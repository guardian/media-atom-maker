import PropTypes from 'prop-types';
import React from 'react';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

export default class GridImage extends React.Component {
  static propTypes = {
    image: PropTypes.object.isRequired
  };

  renderImage() {
    const maybeImageAsset = findSmallestAssetAboveWidth(
      this.props.image?.assets ?? []
    );

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
