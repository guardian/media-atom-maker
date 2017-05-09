import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import TextArea from '../FormFields/TextArea';
import SelectBox from '../FormFields/SelectBox';
import CheckBox from '../FormFields/CheckBox';
import DatePicker from '../FormFields/DatePicker';
import { fieldLengths } from '../../constants/videoEditValidation';
import { videoCategories } from '../../constants/videoCategories';

export default class VideoMetaData extends React.Component {
  render() {
    return (
      <div className="form__group">
        <ManagedForm
          data={this.props.video}
          updateData={this.props.updateVideo}
          editable={this.props.editable}
          updateErrors={this.props.updateErrors}
          formName={this.props.formName}
        >
          <ManagedField
            fieldLocation="title"
            name="Title"
            maxLength={fieldLengths.title}
            isRequired={true}
          >
            <TextInput />
          </ManagedField>
          <ManagedField
            fieldLocation="description"
            name="Description"
            placeholder="No Description"
            customValidation={this.props.descriptionValidator}
            isDesired={true}
          >
            <TextArea />
          </ManagedField>
          <ManagedField
            fieldLocation="blockAds"
            name="Block ads"
            fieldDetails="Ads will not be displayed with this video"
          >
            <CheckBox />
          </ManagedField>
          <ManagedField fieldLocation="category" name="Category">
            <SelectBox selectValues={videoCategories} />
          </ManagedField>
          <ManagedField fieldLocation="expiryDate" name="Expiry Date">
            <DatePicker />
          </ManagedField>
          <ManagedField
            fieldLocation="legallySensitive"
            name="Legally Sensitive"
            fieldDetails="This content involves active criminal proceedings."
          >
            <CheckBox />
          </ManagedField>
        </ManagedForm>
      </div>
    );
  }
}
