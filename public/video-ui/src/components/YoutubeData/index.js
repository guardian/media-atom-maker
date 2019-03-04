import React from 'react';
import { ManagedField, ManagedForm } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import { fieldLengths } from '../../constants/videoEditValidation';
import VideoUtils from '../../util/video';
import TagTypes from '../../constants/TagTypes';
import TagPicker from '../FormFields/TagPicker';
import SelectBox from '../FormFields/SelectBox';
import PrivacyStates from '../../constants/privacyStates';

class YoutubeData extends React.Component {
  render() {
    const {
      video,
      editable,
      updateVideo,
      updateErrors,
      updateWarnings,
      formName
    } = this.props;

    if (!video || !video.id) {
      return;
    }

    const { categories } = this.props.youtube;

    const isYoutubeAtom = VideoUtils.isYoutube(video);
    const availableChannels = VideoUtils.getAvailableChannels(video);
    const availablePrivacyStates = VideoUtils.getAvailablePrivacyStates(video);
    const hasYoutubeWriteAccess = VideoUtils.hasYoutubeWriteAccess(video);

    return (
      <div className="form__group">
        <ManagedForm
          data={video}
          updateData={updateVideo}
          editable={editable}
          updateErrors={updateErrors}
          updateWarnings={updateWarnings}
          formName={formName}
          formClass="atom__edit__form"
        >
          <ManagedField fieldLocation="channelId" name="Channel">
            <SelectBox selectValues={availableChannels} />
          </ManagedField>
          <ManagedField fieldLocation="youtubeCategoryId" name="Category">
            <SelectBox selectValues={categories} />
          </ManagedField>
          <ManagedField
            fieldLocation="privacyStatus"
            name="Privacy Status"
            disabled={!isYoutubeAtom || !hasYoutubeWriteAccess}
          >
            <SelectBox
              selectValues={PrivacyStates.forForm(availablePrivacyStates)}
            />
          </ManagedField>
          <ManagedField fieldLocation="youtubeTitle" name="Title">
            <TextInput />
          </ManagedField>
          <ManagedField
            fieldLocation="youtubeDescription"
            name="Description"
            maxCharLength={fieldLengths.description.charMax}
            maxLength={fieldLengths.description.max}
          >
            <TextInput />
          </ManagedField>
          <ManagedField
            fieldLocation="tags"
            name="Keywords"
            placeholder="No keywords"
            tagType={TagTypes.youtube}
            disabled={!isYoutubeAtom}
            customValidation={this.props.validateYouTubeKeywords}
          >
            <TagPicker disableCapiTags />
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
    youtubeActions: bindActionCreators(
      Object.assign({}, getCategories, getChannels),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(YoutubeData);
