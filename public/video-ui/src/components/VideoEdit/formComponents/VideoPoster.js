import React from 'react';
import GridImageSelect from '../../utils/GridImageSelect';

export default class VideoPosterImageEdit extends React.Component {

  onUpdatePosterImage = (e) => {
    
    console.log(e);

    // let newData = Object.assign({}, this.props.video.data, {
    //   posterUrl: e.target.value
    // });

    // this.props.updateVideo(Object.assign({}, this.props.video, {
    //   data: newData
    // }));
  };

  render () {
    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
        <div className="form__row">
          <label className="form__label">Poster image</label>
          <GridImageSelect onEmbed={this.onUpdatePosterImage}/>
        </div>
    );
  }
}


