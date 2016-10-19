import React from 'react';

import { videoCategories } from '../../../constants/videoCategories';

export default class VideoCategorySelect extends React.Component {

  constructor(props) {
    super(props);
  }

  updateVideoCategory = (e) => {
    let newData = Object.assign({}, this.props.video.data, {
      category: e.target.value
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  render () {
    return (
      <div className="form__row">
        <label className="form__label">Category</label>
        <select {...this.props.input} className="form__field form__field--select" value={this.props.video.data.category || ""} onChange={this.updateVideoCategory}>
          <option value=''></option>
          {videoCategories.map(function(category) {
            return (
                <option value={category} key={category}>{category}</option>
            );
          })}
        </select>
      </div>
    );
  }
}
