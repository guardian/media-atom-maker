import React from 'react';

import { videoCategories } from '../../../constants/videoCategories';

export default class VideoCategorySelect extends React.Component {

  updateVideoCategory = (e) => {
    let newData = Object.assign({}, this.props.video, {
      category: e.target.value
    });

    this.props.updateVideo(newData);
  };

  render () {
    const hasError = this.props.meta.touched && this.props.meta.error;

    return (
      <div className="form__row">
        <label className="form__label">Category</label>
        <select {...this.props.input} className={"form__field form__field--select " + (hasError ? "form__field--error" : "") } value={this.props.video.category || ''} onChange={this.updateVideoCategory}>
          <option value=''></option>
          {videoCategories.map(function(category) {
            return (
                <option value={category} key={category}>{category}</option>
            );
          })}
        </select>
        {hasError ? <p className="form__message form__message--error">{this.props.meta.error}</p> : ""}
      </div>
    );
  }
}
