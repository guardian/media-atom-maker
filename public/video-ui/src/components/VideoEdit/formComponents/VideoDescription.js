import React from 'react';
import TextArea from '../../FormFields/TextArea';

export default class VideoDescriptionEdit extends React.Component {

  onUpdateDescription = (e) => {

    const newValue = e.target.value !== "" ? e.target.value : null;
    if (newValue !== '') {
      const newData = Object.assign({}, this.props.video, {
        description: newValue
      });

      this.props.updateVideo(newData);
    }
  };

  getFieldValue = () => {
    if (this.props.video && this.props.video.description) {
      return this.props.video.description;
    }

    if (!this.props.editable) {
      return 'No Description';
    }

    return '';
  };


  render () {
    return (
        <TextArea
          fieldName="Description"
          fieldValue={this.getFieldValue()}
          noValue={(!this.props.video || !this.props.video.description) ? true : false}
          onUpdateField={this.onUpdateDescription}
          video={this.props.video}
          editable={this.props.editable}
          input={this.props.input}
          meta={this.props.meta} />
    );
  }
}
