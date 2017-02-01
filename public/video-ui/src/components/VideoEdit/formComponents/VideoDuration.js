import React from 'react';
import TextInput from '../../FormFields/TextInput';
import Logger from '../../../logger';

export default class VideoDurationEdit extends React.Component {

  render () {
    if (!this.props.video) {
      Logger.log('VideoEdit loaded without video provided');
      return false;
    }

    return (
        <TextInput
          fieldName="Duration (in seconds)"
          fieldValue={this.props.video.duration}
          inputType="number"
          video={this.props.video}
          editable={this.props.editable}
          input={this.props.input}
          meta={this.props.meta} />
    );
  }
}
