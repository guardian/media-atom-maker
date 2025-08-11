import PropTypes from 'prop-types';
import React from 'react';
import FieldNotification from '../../constants/FieldNotification';
import { formNames } from '../../constants/formNames';
import PrivacyStates from '../../constants/privacyStates';
import TagTypes from '../../constants/TagTypes';
import { fieldLengths } from '../../constants/videoEditValidation';
import YouTubeKeywords from '../../constants/youTubeKeywords';
import KeywordsApi from '../../services/KeywordsApi';
import { Video } from '../../services/VideosApi';
import { YouTubeChannelWithData, YouTubeVideoCategory } from '../../services/YoutubeApi';
import { getYouTubeTagCharCount } from '../../util/getYouTubeTagCharCount';
import VideoUtils from '../../util/video';
import SelectBox from '../FormFields/SelectBox';
import TagPicker from '../FormFields/TagPicker';
import TextAreaInput from '../FormFields/TextAreaInput';
import TextInput from '../FormFields/TextInput';
import { ManagedField, ManagedForm } from '../ManagedForm';

type Props = {
  youtubeActions: {
    getCategories: { (): Promise<void> };
    getChannels: { (): Promise<void> };
  },
  youtube: {
    categories: YouTubeVideoCategory[],
    channels: YouTubeChannelWithData[],
  },
  video: Video,

  editable: boolean,
  updateVideo: { (video: Video): Promise<void> },
  updateErrors: { (errors: Record<string, Record<string, string>>): void },
  updateWarnings: { (warnings: Record<string, boolean>): void }

}

class YoutubeFurniture extends React.Component<Props> {
  constructor(props: Props) {
    super(props);
    if (!this.hasCategories()) {
      this.props.youtubeActions.getCategories();
    }
    if (!this.hasChannels()) {
      this.props.youtubeActions.getChannels();
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

  validateYouTubeDescription = (description: string) => {
    return description && description.match(/<|>/)
      ? new FieldNotification(
        'required',
        `'<' and '>' are not allowed in YouTube descriptions`,
        FieldNotification.error
      )
      : null;
  };

  validateYouTubeKeywords = (youTubeKeywords?: string[]) => {
    const charLimit = YouTubeKeywords.maxCharacters;
    const numberOfChars: number = getYouTubeTagCharCount(youTubeKeywords);
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
        const newVideo = Object.assign({}, video, { tags: oldTags.concat(keywordsToCopy) });
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
    // the parser is interpreting the destructuring pattern in getAvailablePrivacyStates and hasYoutubeWriteAccess
    // as implying the properties of Video are required, but the functions actually allow for them being undefined.
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
import { bindActionCreators, Dispatch } from 'redux';
import { getCategories } from '../../actions/YoutubeActions/getCategories';
import { getChannels } from '../../actions/YoutubeActions/getChannels';
import { KnownAction } from '../../actions';

function mapStateToProps(state: { youtube: Props['youtube'] }) {
  return {
    youtube: state.youtube
  };
}

const actionCreators = {
  getCategories,
  getChannels
};

function mapDispatchToProps(dispatch: Dispatch<KnownAction>) {
  return {
    youtubeActions: bindActionCreators<typeof actionCreators, Props['youtubeActions']>(
      actionCreators,
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(YoutubeFurniture);
