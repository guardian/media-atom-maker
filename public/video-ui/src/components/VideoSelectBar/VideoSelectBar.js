import React from 'react';
import {saveStateVals} from '../../constants/saveStateVals';

export default class VideoSelectBar extends React.Component {

  renderItemImage() {
    if (!this.props.video) {
      return  false;
    }

    if (this.props.video.posterImage) {
      return  <img src={this.props.video.posterImage.master.file} alt={this.props.video.title}/>
    }

    return <div className="grid__image__placeholder">No Image</div>
  }

  render() {

    return (
      <div className="bar info-bar">
      <div className="bar__image">{this.renderItemImage()}</div>
      <div>
        <span className="grid__item__title">{this.props.video.title}</span>
        <button type="button" className="bar__button" onClick={this.props.onSelectVideo}>Select this Video</button>
      </div>
      </div>
    )
  }
}
