import React from 'react';
import SelectBox from '../../FormFields/SelectBox';
import { videoCategories } from '../../../constants/videoCategories';

export default class VideoCategorySelect extends React.Component {

  updateVideoCategory = (e) => {
    let newData = Object.assign({}, this.props.video, {
      category: e.target.value
    });

    this.props.updateVideo(newData);
  };

  render () {
    return (
        <SelectBox
          fieldName="Category"
          fieldValue={this.props.video.category}
          selectValues={videoCategories || []}
          onUpdateField={this.updateVideoCategory}
          {...this.props} />
    );
  }
}
