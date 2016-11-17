import React from 'react';
import TextInput from '../../FormFields/TextInput';

export default class VideoDurationEdit extends React.Component {

  onUpdateDuration = (e) => {
    let newData = Object.assign({}, this.props.video.data, {
      duration: e.target.value
    });

    this.props.updateVideo(Object.assign({}, this.props.video, {
      data: newData
    }));
  };

  render () {
    if (!this.props.video) {
      console.log('VideoEdit loaded without video provided');
      return false;
    }

    return (
        <TextInput
          fieldName="Duration (in seconds)"
          fieldValue={this.props.video.data.duration}
          onUpdateField={this.onUpdateDuration}
          inputType="number"
          {...this.props} />
    );
  }
}
