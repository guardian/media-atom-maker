import React from 'react';
import TextInput from '../../FormFields/TextInput';

export default class VideoTitleEdit extends React.Component {

  onUpdateTitle = (e) => {
    let newData = Object.assign({}, this.props.video, {
      title: e.target.value
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
          fieldName="Title"
          fieldValue={this.props.video.title}
          onUpdateField={this.onUpdateTitle}
          {...this.props} />
    );
  }
}
