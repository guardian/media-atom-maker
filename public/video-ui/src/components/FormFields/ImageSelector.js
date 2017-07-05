import React from 'react';
import { findSmallestAssetAboveWidth } from '../../util/imageHelpers';
import GridImageSelect from '../utils/GridImageSelect';

class ImageSelector extends React.Component {
  renderImage() {
    if (!this.props.fieldValue) {
      return false;
    }

    const image = findSmallestAssetAboveWidth(this.props.fieldValue.assets);

    return (
      <div className="form__image">
        <img src={image.file} />
      </div>
    );
  }

  render() {
    if (this.props.fieldValue === this.props.placeholder) {
      return <div className="form__section">Poster image not selected</div>;
    }
    return (
      <div className="form__row">
        <div className="form__imageselect">
          {this.renderImage()}
        </div>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';

function mapStateToProps(state) {
  return {
    config: state.config
  };
}

export default connect(mapStateToProps)(ImageSelector);
