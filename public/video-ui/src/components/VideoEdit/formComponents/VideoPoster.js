import React from 'react';
import GridImageSelect from '../../utils/GridImageSelect';
import {parseImageFromGridCrop} from '../../../util/parseGridMetadata';
import {findSmallestAsset} from '../../../util/imageHelpers';

class VideoPosterImageEdit extends React.Component {

  onUpdatePosterImage = (cropData) => {

    const image = parseImageFromGridCrop(cropData);

    const newData = Object.assign({}, this.props.video.data, {
      posterImage: image
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  renderImage() {
    if (!this.props.video.data.posterImage) {
      return false;
    }

    const image = findSmallestAsset(this.props.video.data.posterImage.assets)

    return (
      <div className="form__image" >
        <img src={image.url}/>
      </div>
    );
  }

  render () {
    return (
        <div className="form__row">
          <label className="form__label">Poster image</label>
          {this.renderImage()}
          <GridImageSelect onEmbed={this.onUpdatePosterImage} gridUrl={this.props.config.gridUrl}/>
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

export default connect(mapStateToProps)(VideoPosterImageEdit);



