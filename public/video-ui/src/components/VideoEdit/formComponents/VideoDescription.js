import React from 'react';
import TextInput from '../../FormFields/TextInput';

export default class VideoDescriptionEdit extends React.Component {

  onUpdateDescription = (e) => {
    const newData = Object.assign({}, this.props.video, {
      description: e.target.value
    });

    this.props.updateVideo(newData);
  };

  getFieldValue = () => {
    if (this.props.video.description) {
      return this.props.video.description;
    }

    if (!this.props.editable) {
      return 'No Description';
    }

    return ''
  };


  render () {
    return (
        <TextInput
          fieldName="Description"
          fieldValue={this.getFieldValue()}
          noValue={(!this.props.video.description) ? true : false}
          onUpdateField={this.onUpdateDescription}
          video={this.props.video}
          editable={this.props.editable}
          input={this.props.input}
          meta={this.props.meta} />
    );
  }
}
