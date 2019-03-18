import React from 'react';
import PropTypes from 'prop-types';
import { ManagedForm, ManagedField, ManagedSection } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import DurationInput from '../FormFields/DurationInput';
import ScribeEditorField from '../FormFields/ScribeEditor';
import SelectBox from '../FormFields/SelectBox';
import DatePicker from '../FormFields/DatePicker';
import TagPicker from '../FormFields/TagPicker';
import TagTypes from '../../constants/TagTypes';
import { fieldLengths } from '../../constants/videoEditValidation';
import { videoCategories } from '../../constants/videoCategories';
import PrivacyStates from '../../constants/privacyStates';
import VideoUtils from '../../util/video';
import DurationReset from "../DurationReset";
import Flags from "../Flags";
import ContentChangeDetails from "../ContentChangeDetails";
import {formNames} from "../../constants/formNames";

class VideoData extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    updateVideo: PropTypes.func.isRequired,
    editable: PropTypes.bool.isRequired,
    updateErrors: PropTypes.func.isRequired,
    updateWarnings: PropTypes.func.isRequired,
    canonicalVideoPageExists: PropTypes.bool.isRequired,
    validateKeywords: PropTypes.func.isRequired,
    composerKeywordsToYouTube: PropTypes.func.isRequired,
    validateYouTubeKeywords: PropTypes.func.isRequired
  };

  hasCategories = () => this.props.youtube.categories.length !== 0;
  hasChannels = () => this.props.youtube.channels.length !== 0;

  componentWillMount() {
    if (!this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.getChannels();
    }
  }

  render() {
    const {
      video,
      updateVideo,
      updateErrors,
      updateWarnings,
      editable,
      canonicalVideoPageExists,
      validateKeywords,
      composerKeywordsToYouTube,
      validateYouTubeKeywords
    } = this.props;

    const isYoutubeAtom = VideoUtils.isYoutube(video);
    const isCommercialType = VideoUtils.isCommercialType(video);
    const hasAssets = VideoUtils.hasAssets(video);
    const availableChannels = VideoUtils.getAvailableChannels(video);
    const availablePrivacyStates = VideoUtils.getAvailablePrivacyStates(video);
    const hasYoutubeWriteAccess = VideoUtils.hasYoutubeWriteAccess(video);

    const { categories } = this.props.youtube;

    return (
      <div className="form__group">
        <ManagedForm
          data={video}
          updateData={updateVideo}
          editable={editable}
          updateErrors={updateErrors}
          updateWarnings={updateWarnings}
          formName={formNames.videoData}
          formClass="atom__edit__form"
        >
          <ManagedSection>
            <ContentChangeDetails video={video}/>
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
              derivedFrom={video.description}
              name="Trail Text"
              maxCharLength={fieldLengths.description.charMax}
              maxLength={fieldLengths.description.max}
              isDesired={!canonicalVideoPageExists}
              isRequired={canonicalVideoPageExists}
            >
              <ScribeEditorField
                allowedEdits={['bold', 'italic']}
                isDesired={!canonicalVideoPageExists}
                isRequired={canonicalVideoPageExists}
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
              isDesired={!canonicalVideoPageExists}
              isRequired={canonicalVideoPageExists}
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
              customValidation={validateKeywords}
              updateSideEffects={composerKeywordsToYouTube}
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
            <ManagedField fieldLocation="youtubeCategoryId" name="YouTube Category">
              <SelectBox selectValues={categories} />
            </ManagedField>
            <ManagedField
              fieldLocation="tags"
              name="YouTube Keywords"
              placeholder="No keywords"
              tagType={TagTypes.youtube}
              disabled={!isYoutubeAtom}
              customValidation={validateYouTubeKeywords}
            >
              <TagPicker disableCapiTags />
            </ManagedField>
            <Flags
              video={video}
              editable={editable}
              updateVideo={updateVideo}
              updateErrors={updateErrors}
              updateWarnings={updateWarnings}
            />
            <ManagedField fieldLocation="duration" name="Video Duration (mm:ss)">
              <DurationInput />
            </ManagedField>
            {!editable && (
              <DurationReset video={video} updateVideo={updateVideo}/>
            )}
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
    youtube: state.youtube,
    workflow: state.workflow
  };
}

function mapDispatchToProps(dispatch) {
  return {
    youtubeActions: bindActionCreators(
      Object.assign({}, getCategories, getChannels),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(VideoData);
