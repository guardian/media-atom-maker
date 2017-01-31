import React from 'react';
import TextInput from '../../FormFields/TextInput';

export default class VideoTitleEdit extends React.Component {

  onUpdateTitle = (e) => {
    let newData = Object.assign({}, this.props.video, {
      title: e.target.value
    });

    this.props.saveAndUpdateVideo(newData);
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
          video={this.props.video}
          editable={this.props.editable}
          input={this.props.input}
          meta={this.props.meta} />
    );
  }
}
