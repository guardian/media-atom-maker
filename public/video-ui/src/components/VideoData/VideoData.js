import React from 'react';
import { ManagedForm, ManagedField, ManagedSection } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import ScribeEditorField from '../FormFields/ScribeEditor';
import SelectBox from '../FormFields/SelectBox';
import CheckBox from '../FormFields/CheckBox';
import DatePicker from '../FormFields/DatePicker';
import KeywordPicker from '../FormFields/KeywordPicker';
import ComposerTagPicker from '../FormFields/ComposerTagPicker';
import { fieldLengths } from '../../constants/videoEditValidation';
import { videoCategories } from '../../constants/videoCategories';
import { privacyStates } from '../../constants/privacyStates';

class VideoData extends React.Component {
  hasCategories = () => this.props.youtube.categories.length !== 0;
  hasChannels = () => this.props.youtube.channels.length !== 0;
  hasPlutoProjects = () => this.props.pluto.projects.length !== 0;

  componentWillMount() {
    if (!this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
    if (!this.hasPlutoProjects()) {
      this.props.plutoActions.getProjects();
    }
  }

  render() {
    return (
      <div className="form__group">
        <ManagedForm
          data={this.props.video}
          updateData={this.props.updateVideo}
          editable={this.props.editable}
          updateErrors={this.props.updateErrors}
          formName={this.props.formName}
          formClass="atom__edit__form"
        >
          <ManagedSection>
            <ManagedField
              fieldLocation="title"
              name="Headline (YouTube title)"
              maxLength={fieldLengths.title}
              isRequired={true}
            >
              <TextInput />
            </ManagedField>
            <ManagedField
              fieldLocation="description"
              name="Standfirst (YouTube description)"
              customValidation={this.props.descriptionValidator}
              isDesired={true}
              maxCharLength={fieldLengths.description.charMax}
              maxLength={fieldLengths.description.max}
            >
              <ScribeEditorField />
            </ManagedField>
            <ManagedField
              fieldLocation="trailText"
              derivedFrom={this.props.video.description}
              name="Trail Text"
              maxCharLength={fieldLengths.description.charMax}
              maxLength={fieldLengths.description.max}
            >
              <ScribeEditorField />
            </ManagedField>

            <ManagedField
              fieldLocation="byline"
              name="Byline"
              formRowClass="form__row__byline"
              tagType="contributor"
            >
              <ComposerTagPicker />
            </ManagedField>
            <ManagedField
              fieldLocation="commissioningDesks"
              name="Comissioning Desks"
              formRowClass="form__row__byline"
              tagType="tracking"
              inputPlaceholder="Search commissioning info (type '*' to show all)"
            >
              <ComposerTagPicker disableTextInput />
            </ManagedField>

            <ManagedField
              fieldLocation="keywords"
              name="Composer Keywords"
              formRowClass="form__row__byline"
              tagType="keyword"
              inputPlaceholder="Search keywords (type '*' to show all)"
            >
              <ComposerTagPicker disableTextInput />
            </ManagedField>
            <ManagedField
              fieldLocation="source"
              name="Video Source"
            >
              <TextInput />
            </ManagedField>

          </ManagedSection>
          <ManagedSection>
            <ManagedField
              fieldLocation="sensitive"
              name="Sensitive"
              fieldDetails="Contains sensitive content"
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="legallySensitive"
              name="Legally Sensitive"
              fieldDetails="This content involves active criminal proceedings."
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="blockAds"
              name="Block ads"
              fieldDetails="Ads will not be displayed with this video"
            >
              <CheckBox />
            </ManagedField>
            <ManagedField fieldLocation="expiryDate" name="Expiry Date">
              <DatePicker />
            </ManagedField>
            <ManagedField fieldLocation="category" name="Category">
              <SelectBox selectValues={videoCategories} />
            </ManagedField>
            <ManagedField fieldLocation="privacyStatus" name="Privacy Status">
              <SelectBox selectValues={privacyStates} />
            </ManagedField>
            <ManagedField fieldLocation="tags" name="YouTube Keywords">
              <KeywordPicker />
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
import * as getProjects from '../../actions/PlutoActions/getProjects';

function mapStateToProps(state) {
  return {
    youtube: state.youtube,
    pluto: state.pluto
  };
}

function mapDispatchToProps(dispatch) {
  return {
    youtubeActions: bindActionCreators(
      Object.assign({}, getCategories, getChannels),
      dispatch
    ),
    plutoActions: bindActionCreators(Object.assign({}, getProjects), dispatch)
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoData);
