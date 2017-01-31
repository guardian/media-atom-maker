import React from 'react';
import TextInput from '../../FormFields/TextInput';
import Logger from '../../../logger';

export default class VideoTitleEdit extends React.Component {

  onUpdateTitle = (e) => {
    const newData = Object.assign({}, this.props.video, {
      title: e.target.value
    });

    this.props.updateVideo(newData);
  };

  render () {
    if (!this.props.video) {
      Logger.log('VideoEdit loaded without video provided');
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
