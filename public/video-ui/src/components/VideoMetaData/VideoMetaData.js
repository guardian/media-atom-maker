import React from 'react';
import {ManagedForm, ManagedField} from '../ManagedForm';
import FormTextInput from '../FormFields/FormTextInput';
import FormTextArea from '../FormFields/FormTextArea';
import FormSelectBox from '../FormFields/FormSelectBox';
import FormCheckBox from '../FormFields/FormCheckBox';
import FormDatePicker from '../FormFields/FormDatePicker';
import {fieldLengths} from '../../constants/videoEditValidation';
import {videoCategories} from '../../constants/videoCategories';

export default class VideoMetaData extends React.Component {

  render () {
    return (
      <div className="form__group">
        <ManagedForm
          data={this.props.video}
          updateData={this.props.updateVideo}
          editable={this.props.editable}
          updateFormErrors={this.props.updateFormErrors}
        >
          <ManagedField
            fieldLocation="title"
            name="Title"
            maxLength={fieldLengths.title}
            isRequired={true}
          >
            <FormTextInput/>
          </ManagedField>
          <ManagedField
            fieldLocation="description"
            name="Description"
            placeholder="No Description"
          >
            <FormTextArea/>
          </ManagedField>
          <ManagedField
            fieldLocation="category"
            name="Category"
          >
            <FormSelectBox selectValues={videoCategories}></FormSelectBox>
          </ManagedField>
          <ManagedField
            fieldLocation="expiryDate"
            name="Expiry Date"
          >
            <FormDatePicker/>
          </ManagedField>
          <ManagedField
            fieldLocation="legallySensitive"
            name="Legally Sensitive"
          >
            <FormCheckBox/>
          </ManagedField>
        </ManagedForm>
      </div>
    );
  }
};
