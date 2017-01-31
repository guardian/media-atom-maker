import React from 'react';
import GridImageSelect from '../../utils/GridImageSelect';
import {parseImageFromGridCrop} from '../../../util/parseGridMetadata';
import {findSmallestAssetAboveWidth} from '../../../util/imageHelpers';

class VideoPosterImageEdit extends React.Component {

  onUpdatePosterImage = (cropData) => {

    const image = parseImageFromGridCrop(cropData);

    const newData = Object.assign({}, this.props.video, {
      posterImage: image
    });

    this.props.saveAndUpdateVideo(newData);
  };

  renderImage() {
    if (!this.props.video.posterImage) {
      return false;
    }

    const image = findSmallestAssetAboveWidth(this.props.video.posterImage.assets);

    return (
      <div className="form__image" >
        <img src={image.file}/>
      </div>
    );
  }

  render () {

    if (!this.props.editMode) {
      return (
          <div className="form__row">
            <label className="form__label">Poster image</label>
            <div>
              {this.renderImage()}
              <GridImageSelect onEmbed={this.onUpdatePosterImage} gridUrl={this.props.config.gridUrl}/>
            </div>
          </div>
      );
    } else {
      return (
          <div className="form__row">
            <div>
              {this.renderImage()}
              <GridImageSelect onEmbed={this.onUpdatePosterImage} gridUrl={this.props.config.gridUrl}/>
            </div>
          </div>
      );
    }
  }
}


//REDUX CONNECTIONS
import { connect } from 'react-redux';

function mapStateToProps(state) {
  return {
    config: state.config
  };
}

export default connect(mapStateToProps)(VideoPosterImageEdit);
