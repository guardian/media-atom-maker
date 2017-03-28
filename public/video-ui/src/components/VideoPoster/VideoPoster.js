import React from 'react';
import VideoPosterEdit from '../VideoEdit/formComponents/VideoPoster';
import {ManagedForm, ManagedField} from '../ManagedForm';
import ImageSelector from '../FormFields/ImageSelector';

export default class VideoPoster extends React.Component {

  render () {
    return (
      <ManagedForm
        data={this.props.video}
        updateData={this.props.updateVideo}
        editable={this.props.editable}
      >
        <ManagedField
          fieldLocation="posterImage"
          name=""
        >
          <ImageSelector editMode={true}/>
        </ManagedField>
      </ManagedForm>
    );
  }
};
