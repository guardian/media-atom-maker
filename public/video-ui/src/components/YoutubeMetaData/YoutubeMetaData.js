import React from 'react';
import {ManagedForm, ManagedField} from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import ItemPicker from '../FormFields/ItemPicker';
import { privacyStates } from '../../constants/privacyStates';

class YoutubeMetaData extends React.Component {

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
          updateFormErrors={this.props.updateFormErrors}
        >
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
            <ItemPicker/>
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

export default connect(mapStateToProps, mapDispatchToProps)(YoutubeMetaData);
