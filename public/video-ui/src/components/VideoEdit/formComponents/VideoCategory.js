import React from 'react';
import SelectBox from '../../FormFields/SelectBox';
import { videoCategories } from '../../../constants/videoCategories';

export default class VideoCategorySelect extends React.Component {

  defaultOption = "Select a category";

  updateVideoCategory = (e) => {

    if (e.target.value === this.defaultOption) {
      return;
    }

    const newData = Object.assign({}, this.props.video, {
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
          defaultOption={this.defaultOption}
          video={this.props.video}
          editable={this.props.editable}
          input={this.props.input}
          meta={this.props.meta} />
    );
  }
}
