import React from 'react';
import { ManagedForm, ManagedField, ManagedSection } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import ScribeEditorField from '../FormFields/ScribeEditor';
import SelectBox from '../FormFields/SelectBox';
import CheckBox from '../FormFields/CheckBox';
import DatePicker from '../FormFields/DatePicker';
import TagPicker from '../FormFields/TagPicker';
import TagTypes from '../../constants/TagTypes';
import { fieldLengths } from '../../constants/videoEditValidation';
import { videoCategories } from '../../constants/videoCategories';
import { privacyStates } from '../../constants/privacyStates';
import { channelAllowed } from '../../util/channelAllowed';
import { getStore } from '../../util/storeAccessor';

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
    const isHosted = this.props.video.category === 'Hosted';
    const notOnManagedChannel =
      isHosted ||
      !channelAllowed(this.props.video, this.props.youtube.channels);
    const hasAssets = this.props.video.assets.length > 0;

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
              name={
                notOnManagedChannel ? 'Headline' : 'Headline (YouTube title)'
              }
              maxLength={fieldLengths.title}
              isRequired={true}
            >
              <TextInput />
            </ManagedField>
            <ManagedField
              fieldLocation="description"
              name={
                notOnManagedChannel
                  ? 'Standfirst'
                  : 'Standfirst (YouTube description)'
              }
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
              tagType={TagTypes.contributor}
            >
              <TagPicker />
            </ManagedField>
            <ManagedField
              fieldLocation="commissioningDesks"
              name="Comissioning Desks"
              formRowClass="form__row__byline"
              tagType={TagTypes.tracking}
              inputPlaceholder="Search commissioning info (type '*' to show all)"
            >
              <TagPicker disableTextInput />
            </ManagedField>

            <ManagedField
              fieldLocation="keywords"
              name="Composer Keywords"
              formRowClass="form__row__byline"
              tagType={TagTypes.keyword}
              inputPlaceholder="Search keywords (type '*' to show all)"
            >
              <TagPicker disableTextInput />
            </ManagedField>
            <ManagedField fieldLocation="source" name="Video Source">
              <TextInput />
            </ManagedField>
          </ManagedSection>
          <ManagedSection>
            <ManagedField fieldLocation="expiryDate" name="Expiry Date">
              <DatePicker />
            </ManagedField>
            <ManagedField
              fieldLocation="category"
              name="Category"
              disabled={isHosted && hasAssets}
            >
              <SelectBox selectValues={videoCategories} />
            </ManagedField>
            <ManagedField
              fieldLocation="privacyStatus"
              name="Privacy Status"
              disabled={notOnManagedChannel}
            >
              <SelectBox selectValues={privacyStates} />
            </ManagedField>
            <ManagedField
              fieldLocation="tags"
              name="YouTube Keywords"
              placeholder="No keywords"
              tagType={TagTypes.youtube}
              disabled={notOnManagedChannel}
            >
              <TagPicker disableCapiTags />
            </ManagedField>
          </ManagedSection>
          <ManagedSection>
            <ManagedField
              fieldLocation="blockAds"
              name="Block ads"
              fieldDetails="Ads will not be displayed with this video"
              disabled={notOnManagedChannel}
              tooltip={`Videos less than ${getStore().getState().config.minDurationForAds} seconds will automatically have ads blocked`}
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="composerCommentsEnabled"
              name="Comments"
              fieldDetails="Allow comments on Guardian video page (does not change YouTube)"
              disabled={notOnManagedChannel}
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="optimisedForWeb"
              name="Optimised for Web"
              fieldDetails="Optimised for Web"
              disabled={notOnManagedChannel}
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="sensitive"
              name="Sensitive"
              fieldDetails="Contains sensitive content"
              disabled={notOnManagedChannel}
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="legallySensitive"
              name="Legally Sensitive"
              fieldDetails="This content involves active criminal proceedings"
              disabled={notOnManagedChannel}
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="suppressRelatedContent"
              name="Suppress related content"
              fieldDetails="Suppress related content"
              disabled={notOnManagedChannel}
            >
              <CheckBox />
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
    pluto: state.pluto,
    workflow: state.workflow
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
