import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import ImageSelector from '../FormFields/ImageSelector';

export default class VideoPoster extends React.Component {
  render() {
    return (
      <ManagedForm
        data={this.props.video}
        updateData={this.props.updateVideo}
        editable={this.props.editable}
        updateErrors={this.props.updateErrors}
        formName={this.props.formName}
      >
        <ManagedField fieldLocation="posterImage" name="Poster Image">
          <ImageSelector editMode={true} />
        </ManagedField>
      </ManagedForm>
    );
  }
}
