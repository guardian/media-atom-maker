import React from 'react';
import {ManagedForm, ManagedField} from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import TextArea from '../FormFields/TextArea';
import SelectBox from '../FormFields/SelectBox';
import CheckBox from '../FormFields/CheckBox';
import DatePicker from '../FormFields/DatePicker';
import {fieldLengths} from '../../constants/videoEditValidation';
import {videoCategories} from '../../constants/videoCategories';
import { privacyStates } from '../../constants/privacyStates';
import ImageSelector from '../FormFields/ImageSelector';

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
            <TextInput/>
          </ManagedField>
          <ManagedField
            fieldLocation="description"
            name="Description"
            placeholder="No Description"
          >
            <TextArea/>
          </ManagedField>
          <ManagedField
            fieldLocation="category"
            name="Category"
            isRequired={true}
          >
            <SelectBox selectValues={videoCategories}></SelectBox>
          </ManagedField>
          <ManagedField
            fieldLocation="posterImage"
            name="Poster Image">
            <ImageSelector/>
          </ManagedField>
          <ManagedField
            fieldLocation="youtubeCategoryId"
            name="YouTube Category"
            isRequired={true}
          >
            <SelectBox selectValues={this.props.youtube.categories}></SelectBox>
          </ManagedField>
          <ManagedField
            fieldLocation="expiryDate"
            name="Expiry Date"
          >
            <DatePicker/>
          </ManagedField>
          <ManagedField
            fieldLocation="channelId"
            name="YouTube Channel"
            isRequired={true}
          >
            <SelectBox selectValues={this.props.youtube.channels}></SelectBox>
          </ManagedField>
          <ManagedField
            fieldLocation="privacyStatus"
            name="Privacy Status"
            isRequired={true}
          >
            <SelectBox selectValues={privacyStates}></SelectBox>
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
