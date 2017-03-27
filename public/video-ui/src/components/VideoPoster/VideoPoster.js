import React from 'react';
import VideoPosterEdit from '../VideoEdit/formComponents/VideoPoster';
import validate from '../../constants/posterEditValidation';
import {ManagedForm, ManagedField} from '../ManagedForm';
import FormImageSelector from '../FormFields/FormImageSelector';

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
          <FormImageSelector editMode={true}/>
        </ManagedField>
      </ManagedForm>
    );
  }
};
