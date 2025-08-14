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
import KeywordsApi from '../../services/KeywordsApi';
import { fieldLengths } from '../../constants/videoEditValidation';
import TextInput from '../FormFields/TextInput';
import TextAreaInput from '../FormFields/TextAreaInput';
import {useGetYoutubeCategoriesQuery, useGetYoutubeChannelsQuery} from "../../slices/youtubeSlice";
import {Video} from "../../services/VideosApi";

interface YoutubeFurnitureProps {
  video: Video;
  editable: boolean;
  updateVideo: (video: Video) => void;
  updateErrors: (errors: any) => void;
  updateWarnings: (warnings: any) => void;
}

export const YoutubeFurniture = ({
  video,
  editable,
  updateVideo,
  updateErrors,
  updateWarnings
}: YoutubeFurnitureProps) => {
  const validateYouTubeDescription = (description: string): FieldNotification | null => {
    return description && description.match(/<|>/)
      ? new FieldNotification(
          'required',
          `'<' and '>' are not allowed in YouTube descriptions`,
          FieldNotification.error
        )
      : null;
  };

  const validateYouTubeKeywords = (youTubeKeywords: string[]): FieldNotification | null => {
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

  const composerKeywordsToYouTube = (): Promise<void> => {
    return Promise.all(
      video.keywords.map(keyword => KeywordsApi.composerTagToYouTube(keyword))
    )
      .then((youTubeKeywords: string[][]) => {
        const flattenedKeywords = youTubeKeywords.flat();
        const oldTags = video.tags;
        const keywordsToCopy = flattenedKeywords.reduce((tagsAdded: string[], keyword: string) => {
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

  const { data: categories, isLoading: isCategoriesLoading } = useGetYoutubeCategoriesQuery();
  const { data: channels, isLoading: isChannelsLoading } = useGetYoutubeChannelsQuery();

  if(isCategoriesLoading || isChannelsLoading ) {
    return null;
  }

  const isYoutubeAtom = VideoUtils.isYoutube(video);

  const getAvailableChannels = () => {
    const isCommercialType = VideoUtils.isCommercialType(video);
    return channels.filter(_ => _.isCommercial === isCommercialType);
  }
  const availableChannels = getAvailableChannels();

  const getAvailablePrivacyStates = () => {
    const channel = VideoUtils.getYoutubeChannel(video as { channelId: string });
    return channel ? channel.privacyStates : PrivacyStates.defaultStates;
  }
  const availablePrivacyStates = getAvailablePrivacyStates();

  const checkYoutubeWriteAccess = () => {
    if (
      availablePrivacyStates && !availablePrivacyStates.includes(video.privacyStatus)
    ) {
      return false;
    }
    return !!VideoUtils.getYoutubeChannel(video as { channelId: string });
  }
  const hasYoutubeWriteAccess = checkYoutubeWriteAccess();

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
        customValidation={validateYouTubeDescription}
      >
        <TextAreaInput />
      </ManagedField>
      <ManagedField
        fieldLocation="tags"
        name="Keywords"
        placeholder="No keywords"
        tagType={TagTypes.youtube}
        disabled={!isYoutubeAtom}
        customValidation={validateYouTubeKeywords}
        updateSideEffects={composerKeywordsToYouTube}
      >
        <TagPicker disableCapiTags />
      </ManagedField>
    </ManagedForm>
  );
};
