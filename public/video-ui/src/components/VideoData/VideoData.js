import React from 'react';
import {ManagedForm, ManagedField, ManagedSection} from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import TextArea from '../FormFields/TextArea';
import SelectBox from '../FormFields/SelectBox';
import CheckBox from '../FormFields/CheckBox';
import DatePicker from '../FormFields/DatePicker';
import KeywordPicker from '../FormFields/KeywordPicker';
import {fieldLengths} from '../../constants/videoEditValidation';
import {videoCategories} from '../../constants/videoCategories';
import {privacyStates} from '../../constants/privacyStates';

class VideoData extends React.Component {

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

  render () {
    return (
      <div className="form__group">
        <ManagedForm
          data={this.props.video}
          updateData={this.props.updateVideo}
          editable={this.props.editable}
          updateErrors={this.props.updateErrors}
          formName={this.props.formName}
        >
          <ManagedSection>
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
              customValidation={this.props.descriptionValidator}
              isDesired={true}
            >
              <TextArea/>
            </ManagedField>
            <ManagedField
              fieldLocation="blockAds"
              name="Block ads"
              fieldDetails="Ads will not be displayed with this video"
            >
              <CheckBox/>
            </ManagedField>
            <ManagedField
              fieldLocation="category"
              name="Category"
            >
              <SelectBox selectValues={videoCategories}></SelectBox>
            </ManagedField>
            <ManagedField
              fieldLocation="expiryDate"
              name="Expiry Date"
            >
              <DatePicker/>
            </ManagedField>
          </ManagedSection>
          <ManagedSection>
            <ManagedField
              fieldLocation="youtubeCategoryId"
              name="YouTube Category"
            >
              <SelectBox selectValues={this.props.youtube.categories}></SelectBox>
            </ManagedField>
            <ManagedField
              fieldLocation="channelId"
              name="YouTube Channel"
            >
              <SelectBox selectValues={this.props.youtube.channels}></SelectBox>
            </ManagedField>
            <ManagedField
              fieldLocation="privacyStatus"
              name="Privacy Status"
            >
              <SelectBox selectValues={privacyStates}></SelectBox>
            </ManagedField>
            <ManagedField
              fieldLocation="tags"
              name="Keywords"
            >
              <KeywordPicker/>
            </ManagedField>
          </ManagedSection>
          <ManagedSection>
            <ManagedField
              fieldLocation="legallySensitive"
              name="Legally Sensitive"
              fieldDetails="This content involves active criminal proceedings."
            >
              <CheckBox/>
            </ManagedField>
          </ManagedSection>
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

export default connect(mapStateToProps, mapDispatchToProps)(VideoData);
