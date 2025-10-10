import React from 'react';
import PropTypes from 'prop-types';
import { ManagedField, ManagedForm } from '../ManagedForm';
import VideoUtils from '../../util/video';
import TagTypes from '../../constants/TagTypes';
import TagPicker from '../FormFields/TagPicker';
import SelectBox from '../FormFields/SelectBox';
import PrivacyStates from '../../constants/privacyStates';
import { formNames } from '../../constants/formNames';
import YouTubeKeywords from '../../constants/youTubeKeywords';
import { getYouTubeTagCharCount } from '../../util/getYouTubeTagCharCount';
import FieldNotification from '../../constants/FieldNotification';
import KeywordsApi from '../../services/KeywordsApi';
import { fieldLengths } from '../../constants/videoEditValidation';
import TextInput from '../FormFields/TextInput';
import TextAreaInput from '../FormFields/TextAreaInput';

class YoutubeFurniture extends React.Component {
  constructor(props) {
    super(props);
    if (!this.hasCategories()) {
      this.props.youtubeActions.fetchCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.fetchChannels();
    }
  }

  static propTypes = {
    video: PropTypes.object.isRequired,
    editable: PropTypes.bool.isRequired,
    updateVideo: PropTypes.func.isRequired,
    updateErrors: PropTypes.func.isRequired,
    updateWarnings: PropTypes.func.isRequired
  };

  hasCategories = () => this.props.youtube.categories.length !== 0;
  hasChannels = () => this.props.youtube.channels.length !== 0;

  validateYouTubeDescription = description => {
    return description && description.match(/<|>/)
      ? new FieldNotification(
        'required',
        `'<' and '>' are not allowed in YouTube descriptions`,
        FieldNotification.error
      )
      : null;
  };

  validateYouTubeKeywords = youTubeKeywords => {
    const charLimit = YouTubeKeywords.maxCharacters;
    const numberOfChars = getYouTubeTagCharCount(youTubeKeywords);
    const characterCountExceeded = numberOfChars > charLimit;

    return characterCountExceeded
      ? new FieldNotification(
        'required',
        `Maximum characters allowed in YouTube keywords is ${charLimit}.`,
        FieldNotification.error
      )
      : null;
  };

  composerKeywordsToYouTube = () => {
    const { video, updateVideo } = this.props;

    return Promise.all(
      video.keywords.map(keyword => KeywordsApi.composerTagToYouTube(keyword))
    )
      .then(youTubeKeywords => {
        const oldTags = video.tags;
        const keywordsToCopy = youTubeKeywords.reduce((tagsAdded, keyword) => {
          const allAddedTags = oldTags.concat(tagsAdded);
          if (keyword !== '' && allAddedTags.every(oldTag => oldTag !== keyword)) {
            tagsAdded.push(keyword);
          }
          return tagsAdded;
        }, []);
        const newVideo = Object.assign({}, video, { tags: oldTags.concat(keywordsToCopy)});
        updateVideo(newVideo);
      });
  };

  render() {
    const {
      video,
      editable,
      updateVideo,
      updateErrors,
      updateWarnings
    } = this.props;

    const { categories } = this.props.youtube;

    const isYoutubeAtom = VideoUtils.isYoutube(video);
    const availableChannels = VideoUtils.getAvailableChannels(video);
    const availablePrivacyStates = VideoUtils.getAvailablePrivacyStates(video);
    const hasYoutubeWriteAccess = VideoUtils.hasYoutubeWriteAccess(video);
    const isChannelSelectionDisabled = VideoUtils.hasAssets(video) && video.channelId;

    return (
      <ManagedForm
        data={video}
        updateData={updateVideo}
        editable={editable}
        updateErrors={updateErrors}
        updateWarnings={updateWarnings}
        formName={formNames.youtubeFurniture}
        formClass="atom__edit__form"
      >
        <ManagedField fieldLocation="channelId" name="Channel" disabled={isChannelSelectionDisabled}>
          <SelectBox selectValues={availableChannels} />
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
        <ManagedField fieldLocation="youtubeCategoryId" name="Category">
          <SelectBox selectValues={categories} />
        </ManagedField>
        <ManagedField
          fieldLocation="youtubeTitle"
          name="Title"
          maxLength={fieldLengths.title}
          isRequired={true}
        >
          <TextInput />
        </ManagedField>
        <ManagedField
          fieldLocation="youtubeDescription"
          name="Description"
          maxWordLength={fieldLengths.youtubeDescription.charMax}
          maxLength={fieldLengths.youtubeDescription.charMax}
          isRequired={false}
          customValidation={this.validateYouTubeDescription}
        >
          <TextAreaInput />
        </ManagedField>
        <ManagedField
          fieldLocation="tags"
          name="Keywords"
          placeholder="No keywords"
          tagType={TagTypes.youtube}
          disabled={!isYoutubeAtom}
          customValidation={this.validateYouTubeKeywords}
          updateSideEffects={this.composerKeywordsToYouTube}
        >
          <TagPicker disableCapiTags />
        </ManagedField>
      </ManagedForm>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import { fetchCategories, fetchChannels } from "../../slices/youtube";

function mapStateToProps(state) {
  return {
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch) {
  return {
    youtubeActions: bindActionCreators(
      { fetchChannels, fetchCategories },
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(YoutubeFurniture);
