import React from 'react';
import {ManagedForm, ManagedField} from '../ManagedForm';
import FormTextInput from '../FormFields/FormTextInput';
import FormTextArea from '../FormFields/FormTextArea';
import FormSelectBox from '../FormFields/FormSelectBox';
import FormCheckBox from '../FormFields/FormCheckBox';
import FormDatePicker from '../FormFields/FormDatePicker';
import {fieldLengths} from '../../constants/videoEditValidation';
import {videoCategories} from '../../constants/videoCategories';
import { privacyStates } from '../../constants/privacyStates';
import FormImageSelector from '../FormFields/FormImageSelector';

class VideoEdit extends React.Component {

  hasCategories = () => this.props.youtube.categories.length !== 0;
  hasChannels = () => this.props.youtube.channels.length !== 0;

  componentWillMount() {
    if (! this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
    if (! this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
  }

  render() {

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
            fieldLocation="posterImage"
            name="Poster Image">
            <FormImageSelector/>
          </ManagedField>
          <ManagedField
            fieldLocation="youtubeCategoryId"
            name="YouTube Category"
          >
            <FormSelectBox selectValues={this.props.youtube.categories}></FormSelectBox>
          </ManagedField>
          <ManagedField
            fieldLocation="expiryDate"
            name="Expiry Date"
          >
            <FormDatePicker/>
          </ManagedField>
          <ManagedField
            fieldLocation="channelId"
            name="YouTube Channel"
          >
            <FormSelectBox selectValues={this.props.youtube.channels}></FormSelectBox>
          </ManagedField>
          <ManagedField
            fieldLocation="privacyStatus"
            name="Privacy Status"
          >
            <FormSelectBox selectValues={privacyStates}></FormSelectBox>
          </ManagedField>
        </ManagedForm>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getCategories from '../../actions/YoutubeActions/getCategories';
import * as getChannels from '../../actions/YoutubeActions/getChannels';

function mapStateToProps(state) {
  return {
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch) {
  return {
    youtubeActions: bindActionCreators(Object.assign({}, getCategories, getChannels), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoEdit);
