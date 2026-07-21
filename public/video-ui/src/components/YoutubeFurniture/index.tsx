import React from 'react';
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
import { addOrDropBundlingTags } from '../../services/KeywordsApi';
import { fieldLengths } from '../../constants/videoEditValidation';
import TextInput from '../FormFields/TextInput';
import TextAreaInput from '../FormFields/TextAreaInput';

type Props = {
  video: Video;
  editable: boolean;
  updateVideo: (...args: any[]) => any;
  updateErrors: (...args: any[]) => any;
  updateWarnings: (...args: any[]) => any;
};

class YoutubeFurniture extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
    if (!this.hasCategories()) {
      (this.props as any).youtubeActions.fetchCategories();
    }
    if (!this.hasChannels()) {
      (this.props as any).youtubeActions.fetchChannels();
    }
  }

  hasCategories = () => (this.props as any).youtube.categories.length !== 0;
  hasChannels = () => (this.props as any).youtube.channels.length !== 0;

  validateYouTubeDescription = (description: string) => {
    return description && description.match(/<|>/)
      ? new FieldNotification(
          'required',
          `'<' and '>' are not allowed in YouTube descriptions`,
          FieldNotification.error
        )
      : null;
  };

  validateYouTubeKeywords = (youTubeKeywords: string[]) => {
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

    const fullTags = addOrDropBundlingTags({
      keywords: video.keywords,
      tags: video.tags,
      blockAds: video.blockAds
    });
    const newVideo = Object.assign({}, video, { tags: fullTags });
    updateVideo(newVideo);
  };

  render() {
    const { video, editable, updateVideo, updateErrors, updateWarnings } =
      this.props;

    const { categories } = (this.props as any).youtube;

    const availableChannels = VideoUtils.getAvailableChannels(video);
    const availablePrivacyStates = VideoUtils.getAvailablePrivacyStates(video);
    const hasYoutubeWriteAccess = VideoUtils.hasYoutubeWriteAccess(video);
    const isChannelSelectionDisabled =
      VideoUtils.hasAssets(video) && video.channelId;
    const platform = VideoUtils.getPlatformFromAtom(video);

    return (
      // @ts-expect-error TS(2769): No overload matches this call.
      <ManagedForm
        data={video}
        updateData={updateVideo}
        editable={editable}
        updateErrors={updateErrors}
        updateWarnings={updateWarnings}
        formName={formNames.youtubeFurniture}
        formClass="atom__edit__form"
      >
        <ManagedField
          fieldLocation="channelId"
          name="Channel"
          // @ts-expect-error TS(2769): No overload matches this call.
          disabled={isChannelSelectionDisabled}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <SelectBox selectValues={availableChannels} />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="privacyStatus"
          name="Privacy Status"
          disabled={platform !== 'youtube' || !hasYoutubeWriteAccess}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <SelectBox
            selectValues={PrivacyStates.forForm(availablePrivacyStates)}
          />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField fieldLocation="youtubeCategoryId" name="Category">
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <SelectBox selectValues={categories} />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="youtubeTitle"
          name="Title"
          maxLength={fieldLengths.title}
          isRequired={true}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <TextInput />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="youtubeDescription"
          name="Description"
          maxWordLength={fieldLengths.youtubeDescription.charMax}
          maxLength={fieldLengths.youtubeDescription.charMax}
          isRequired={false}
          customValidation={this.validateYouTubeDescription}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <TextAreaInput />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="tags"
          name="Keywords"
          placeholder="No keywords"
          tagType={TagTypes.youtube}
          disabled={platform !== 'youtube'}
          customValidation={this.validateYouTubeKeywords}
          updateSideEffects={this.composerKeywordsToYouTube}
        >
          {/* @ts-expect-error TS(2322): Type '{ disableCapiTags: true; }' is not assignabl... Remove this comment to see the full error message */}
          <TagPicker disableCapiTags />
        </ManagedField>
      </ManagedForm>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { AnyAction, bindActionCreators, Dispatch } from 'redux';
import { fetchCategories, fetchChannels } from '../../slices/youtube';
import { Video } from '../../services/VideosApi';

function mapStateToProps(state: { youtube: any }) {
  return {
    youtube: state.youtube
  };
}

function mapDispatchToProps(dispatch: Dispatch<AnyAction>) {
  return {
    youtubeActions: bindActionCreators(
      { fetchChannels, fetchCategories },
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(YoutubeFurniture);
