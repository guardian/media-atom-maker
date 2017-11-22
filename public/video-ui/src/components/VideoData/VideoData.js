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
import PrivacyStates from '../../constants/privacyStates';
import VideoUtils from '../../util/video';

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
    const isYoutubeAtom = VideoUtils.isYoutube(this.props.video);
    const isCommercialType = VideoUtils.isCommercialType(this.props.video);
    const hasAssets = VideoUtils.hasAssets(this.props.video);
    const isEligibleForAds = VideoUtils.isEligibleForAds(this.props.video);
    const availableChannels = VideoUtils.getAvailableChannels(this.props.video);
    const availablePrivacyStates = VideoUtils.getAvailablePrivacyStates(this.props.video);
    const hasYoutubeWriteAccess = VideoUtils.hasYoutubeWriteAccess(this.props.video);

    return (
      <div className="form__group">
        <ManagedForm
          data={this.props.video}
          updateData={this.props.updateVideo}
          editable={this.props.editable}
          updateErrors={this.props.updateErrors}
          updateWarnings={this.props.updateWarnings}
          formName={this.props.formName}
          formClass="atom__edit__form"
        >
          <ManagedSection>
            <ManagedField
              fieldLocation="title"
              name={
                isYoutubeAtom ? 'Headline (YouTube title)' : 'Headline'
              }
              maxLength={fieldLengths.title}
              isRequired={true}
            >
              <TextInput />
            </ManagedField>
            <ManagedField
              fieldLocation="description"
              name={
                isYoutubeAtom
                  ? 'Standfirst (YouTube description)'
                  : 'Standfirst'
              }
              customValidation={this.props.descriptionValidator}
              maxCharLength={fieldLengths.description.charMax}
              maxLength={fieldLengths.description.max}
            >
              <ScribeEditorField
                allowedEdits={['bold', 'italic', 'linkPrompt', 'unlink', 'insertUnorderedList']}
              />
            </ManagedField>
            <ManagedField
              fieldLocation="trailText"
              derivedFrom={this.props.video.description}
              name="Trail Text"
              maxCharLength={fieldLengths.description.charMax}
              maxLength={fieldLengths.description.max}
              isDesired={true}
            >
              <ScribeEditorField
                allowedEdits={['bold', 'italic']}

              />
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
              name="Commissioning Desks"
              formRowClass="form__row__byline"
              tagType={TagTypes.tracking}
              isDesired={true}
              inputPlaceholder="Search commissioning info (type '*' to show all)"
            >
              <TagPicker disableTextInput />
            </ManagedField>

            <ManagedField
              fieldLocation="keywords"
              name="Composer Keywords"
              formRowClass="form__row__byline"
              tagType={isCommercialType ? TagTypes.commercial : TagTypes.keyword}
              isDesired={true}
              inputPlaceholder="Search keywords (type '*' to show all)"
              customValidation={this.props.validateKeywords}
              updateSideEffects={this.props.composerKeywordsToYouTube}
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
              disabled={hasAssets}
            >
              <SelectBox selectValues={videoCategories} />
            </ManagedField>
            <ManagedField fieldLocation="channelId" name="YouTube Channel" disabled={hasAssets}>
              <SelectBox selectValues={availableChannels} />
            </ManagedField>
            <ManagedField
              fieldLocation="privacyStatus"
              name="Privacy Status"
              disabled={!isYoutubeAtom || !hasYoutubeWriteAccess}
            >
              <SelectBox selectValues={PrivacyStates.forForm(availablePrivacyStates)} />
            </ManagedField>
            <ManagedField
              fieldLocation="tags"
              name="YouTube Keywords"
              placeholder="No keywords"
              tagType={TagTypes.youtube}
              disabled={!isYoutubeAtom}
              customValidation={this.props.validateYouTubeKeywords}
            >
              <TagPicker disableCapiTags />
            </ManagedField>
            <ManagedField
              fieldLocation="blockAds"
              name="Block ads"
              fieldDetails="Ads will not be displayed with this video"
              disabled={!isYoutubeAtom || !isEligibleForAds}
              tooltip={!isEligibleForAds ? `Not eligible for pre-roll.` : ''}
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="composerCommentsEnabled"
              name="Comments"
              fieldDetails="Allow comments on Guardian video page (does not change YouTube)"
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="optimisedForWeb"
              name="Optimised for Web"
              fieldDetails="Optimised for Web"
            >
              <CheckBox />
            </ManagedField>
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
              fieldDetails="This content involves active criminal proceedings"
            >
              <CheckBox />
            </ManagedField>
            <ManagedField
              fieldLocation="suppressRelatedContent"
              name="Suppress related content"
              fieldDetails="Suppress related content"
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
