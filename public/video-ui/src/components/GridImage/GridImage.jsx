import React from 'react';
import PropTypes from 'prop-types';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';

export default class GridImage extends React.Component {
  static propTypes = {
    image: PropTypes.object.isRequired
  };

  renderImage() {
    if (!this.props.image || this.props.image.assets.length === 0) {
      return (
        <div>no image</div>
      );
    }

    const image = findSmallestAssetAboveWidth(this.props.image.assets);

    return (
      <div className="form__image">
        <img src={image.file} />
      </div>
    );
  }

  render() {
    return (
      <div className="form__row">
        <div className="form__imageselect">
          {this.renderImage()}
        </div>
      </div>
    );
  }
}
