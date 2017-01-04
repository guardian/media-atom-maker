import React from 'react';
import TextInput from '../../FormFields/TextInput';

export default class VideoDurationEdit extends React.Component {

  onUpdateDuration= (e) => {
    let newData = Object.assign({}, this.props.video, {
      duration: Number(e.target.value)
    });

    this.props.updateVideo(newData);
  };

  render () {
    if (!this.props.video) {
      console.log('VideoEdit loaded without video provided');
      return false;
    }

    return (
        <TextInput
          fieldName="Duration (in seconds)"
          fieldValue={this.props.video.duration}
          onUpdateField={this.onUpdateDuration}
          inputType="number"
          video={this.props.video}
          editable={this.props.editable}
          input={this.props.input}
          meta={this.props.meta} />
    );
  }
}
