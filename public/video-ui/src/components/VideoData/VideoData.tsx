import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import DurationInput from '../FormFields/DurationInput';
import RichTextField from '../FormFields/RichTextField';
import SelectBox from '../FormFields/SelectBox';
import DatePicker from '../FormFields/DatePicker';
import TagPicker from '../FormFields/TagPicker';
import StandTagPicker from '../FormFields/StandTagPicker';
import {
  isTagAllowed,
  supportedTagFilters
} from '../FormFields/tags/allowTags';
import TagTypes from '../../constants/TagTypes';
import { fieldLengths } from '../../constants/videoEditValidation';
import { videoCategories } from '../../constants/videoCategories';
import VideoUtils from '../../util/video';
import { formNames } from '../../constants/formNames';
import FieldNotification from '../../constants/FieldNotification';
import {
  trailTextConfig,
  standfirstConfig
} from '../FormFields/richtext/config';
import { ExpireNowComponent } from '../FormFields/ExpireNow';
import { addOrDropBundlingTags } from '../../services/KeywordsApi';
import { Video } from '../../services/VideosApi';

type Props = {
  video: Video;
  updateVideo: (...args: any[]) => any;
  editable: boolean;
  updateErrors: (...args: any[]) => any;
  updateWarnings: (...args: any[]) => any;
  canonicalVideoPageExists: boolean;
};

export default class VideoData extends React.Component<Props> {
  validateKeywords = (keywords: any) => {
    if (
      !Array.isArray(keywords) ||
      keywords.length === 0 ||
      keywords.every(keyword => {
        return keyword.match(/^tone/);
      })
    ) {
      if (this.props.canonicalVideoPageExists) {
        return new FieldNotification(
          'error',
          'A series or a keyword tag is required for updating composer pages',
          FieldNotification.error
        );
      }
      return new FieldNotification(
        'desired',
        'A series or a keyword tag is required for creating composer pages',
        FieldNotification.warning
      );
    }
    return null;
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
    const {
      video,
      updateVideo,
      updateErrors,
      updateWarnings,
      editable,
      canonicalVideoPageExists
    } = this.props;

    const isCommercialType = VideoUtils.isCommercialType(video);
    const hasAssets = VideoUtils.hasAssets(video);
    const canHaveComposerPage = VideoUtils.canHaveComposerPage(video);
    const mustHaveTags = VideoUtils.mustHaveTags(video);

    // @ts-expect-error TS(2769): No overload matches this call.
    return (
      <ManagedForm
        data={video}
        updateData={updateVideo}
        editable={editable}
        updateErrors={updateErrors}
        updateWarnings={updateWarnings}
        formName={formNames.videoData}
        formClass="atom__edit__form"
      >
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="title"
          name={canHaveComposerPage ? 'Headline' : 'Title'}
          maxLength={fieldLengths.title}
          isRequired={true}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <TextInput />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        {canHaveComposerPage && (
          <ManagedField
            fieldLocation="description"
            name="Standfirst"
            maxLength={fieldLengths.description.charMax}
            maxWordLength={fieldLengths.description.max}
          >
            {/* @ts-expect-error TS(2769): No overload matches this call. */}
            <RichTextField config={standfirstConfig} />
          </ManagedField>
        )}
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        {canHaveComposerPage && (
          <ManagedField
            fieldLocation="trailText"
            derivedFrom={video.description}
            name="Trail Text"
            maxLength={fieldLengths.description.charMax}
            maxWordLength={fieldLengths.description.max}
            isDesired={!canonicalVideoPageExists}
            isRequired={canonicalVideoPageExists}
          >
            {/* @ts-expect-error TS(2769): No overload matches this call. */}
            <RichTextField
              isDesired={!canonicalVideoPageExists}
              isRequired={canonicalVideoPageExists}
              config={trailTextConfig}
            />
          </ManagedField>
        )}
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        {canHaveComposerPage && (
          <ManagedField
            fieldLocation="byline"
            name="Byline"
            formRowClass="form__row__byline"
            tagType={TagTypes.contributor}
          >
            <TagPicker />
          </ManagedField>
        )}
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        {canHaveComposerPage && (
          <ManagedField
            fieldLocation="commissioningDesks"
            name="Commissioning Desks"
            formRowClass="form__row__byline"
            tagType={TagTypes.tracking}
            isDesired={!canonicalVideoPageExists}
            isRequired={canonicalVideoPageExists}
            inputPlaceholder="Search commissioning info (type '*' to show all)"
          >
            {/* @ts-expect-error TS(2322): Type '{ disableTextInput: true; tagSubType: string... Remove this comment to see the full error message */}
            <TagPicker disableTextInput tagSubType="commissioningdesk" />
          </ManagedField>
        )}
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        {video.platform !== 'Youtube' && (
          <ManagedField
            fieldLocation="atomTagIds"
            name="Tags"
            formRowClass="form__row__byline"
            isDesired={mustHaveTags}
            isRequired={false}
            inputPlaceholder="Search tags (type '*' to show all)"
          >
            {/* tagTypes is not really used as we supply filterOptions */}
            {/* @ts-expect-error TS(2739): Type '{ tagTypes: undefined[]; allowTags: (tag: Vi... Remove this comment to see the full error message */}
            <StandTagPicker
              tagTypes={[]}
              allowTags={isTagAllowed}
              filterOptions={supportedTagFilters}
            />
          </ManagedField>
        )}
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        {canHaveComposerPage && (
          <ManagedField
            fieldLocation="keywords"
            name="Composer Keywords"
            formRowClass="form__row__byline"
            tagType={isCommercialType ? TagTypes.commercial : TagTypes.keyword}
            isDesired={true}
            inputPlaceholder="Search keywords (type '*' to show all)"
            customValidation={this.validateKeywords}
            updateSideEffects={this.composerKeywordsToYouTube}
          >
            {/* @ts-expect-error TS(2322): Type '{ disableTextInput: true; }' is not assignab... Remove this comment to see the full error message */}
            <TagPicker disableTextInput />
          </ManagedField>
        )}
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField fieldLocation="source" name="Video Source">
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <TextInput />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField fieldLocation="expiryDate" name="Expiry Date">
          {/* @ts-expect-error TS(2740): Type '{}' is missing the following properties from... Remove this comment to see the full error message */}
          <DatePicker />
          <ExpireNowComponent
            fieldName={(video as any).fieldName}
            editable={editable}
            onUpdateField={(video as any).onUpdateField}
            fieldValue={(video as any).fieldValue}
          />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="category"
          name="Category"
          disabled={hasAssets}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <SelectBox selectValues={videoCategories} />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField fieldLocation="duration" name="Video Duration (mm:ss)">
          {/* @ts-expect-error TS(2740): Type '{}' is missing the following properties from... Remove this comment to see the full error message */}
          <DurationInput />
        </ManagedField>
      </ManagedForm>
    );
  }
}
