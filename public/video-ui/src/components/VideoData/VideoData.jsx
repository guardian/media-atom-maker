import React from 'react';
import PropTypes from 'prop-types';
import { ManagedForm, ManagedField } from '../ManagedForm';
import TextInput from '../FormFields/TextInput';
import DurationInput from '../FormFields/DurationInput';
import RichTextField from '../FormFields/RichTextField';
import SelectBox from '../FormFields/SelectBox';
import DatePicker from '../FormFields/DatePicker';
import TagPicker from '../FormFields/TagPicker';
import TagTypes from '../../constants/TagTypes';
import { fieldLengths } from '../../constants/videoEditValidation';
import { videoCategories } from '../../constants/videoCategories';
import VideoUtils from '../../util/video';
import {formNames} from "../../constants/formNames";
import FieldNotification from "../../constants/FieldNotification";
import { trailTextConfig, standfirstConfig } from "../FormFields/richtext/config";
import { ExpireNowComponent } from "../FormFields/ExpireNow";

export default class VideoData extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    updateVideo: PropTypes.func.isRequired,
    editable: PropTypes.bool.isRequired,
    updateErrors: PropTypes.func.isRequired,
    updateWarnings: PropTypes.func.isRequired,
    canonicalVideoPageExists: PropTypes.bool.isRequired
  };

  validateKeywords = keywords => {
    if (!Array.isArray(keywords) ||
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
    const canHaveComposerPage =
      this.props.video.videoPlayerFormat !== 'Cinemagraph' &&
      this.props.video.videoPlayerFormat !== 'Loop';

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
        <ManagedField
          fieldLocation="title"
          name="Headline"
          maxLength={fieldLengths.title}
          isRequired={true}
        >
          <TextInput />
        </ManagedField>
        { canHaveComposerPage &&
          <ManagedField
          fieldLocation="description"
          name="Standfirst"
          maxLength={fieldLengths.description.charMax}
          maxWordLength={fieldLengths.description.max}
        >
          <RichTextField
            config={standfirstConfig}
          />
        </ManagedField>
        }
        { canHaveComposerPage &&
          <ManagedField
            fieldLocation="trailText"
            derivedFrom={video.description}
            name="Trail Text"
            maxLength={fieldLengths.description.charMax}
            maxWordLength={fieldLengths.description.max}
            isDesired={!canonicalVideoPageExists}
            isRequired={canonicalVideoPageExists}
          >
            <RichTextField
              isDesired={!canonicalVideoPageExists}
              isRequired={canonicalVideoPageExists}
              config={trailTextConfig}
            />
          </ManagedField>
        }
        { canHaveComposerPage &&
          <ManagedField
            fieldLocation="byline"
            name="Byline"
            formRowClass="form__row__byline"
            tagType={TagTypes.contributor}
          >
            <TagPicker />
          </ManagedField>
        }
        {
          canHaveComposerPage &&
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
        }
        {
          canHaveComposerPage &&
            <ManagedField
              fieldLocation="keywords"
              name="Composer Keywords"
              formRowClass="form__row__byline"
              tagType={isCommercialType ? TagTypes.commercial : TagTypes.keyword}
              isDesired={true}
              inputPlaceholder="Search keywords (type '*' to show all)"
              customValidation={this.validateKeywords}
            >
              <TagPicker disableTextInput />
            </ManagedField>
        }
        <ManagedField fieldLocation="source" name="Video Source">
          <TextInput />
        </ManagedField>
        <ManagedField fieldLocation="expiryDate" name="Expiry Date">
          <DatePicker />
          <ExpireNowComponent fieldName={video.fieldName} editable={editable} onUpdateField={video.onUpdateField} fieldValue={video.fieldValue} />
        </ManagedField>
        <ManagedField
          fieldLocation="category"
          name="Category"
          disabled={hasAssets}
        >
          <SelectBox selectValues={videoCategories} />
        </ManagedField>
        <ManagedField fieldLocation="duration" name="Video Duration (mm:ss)">
          <DurationInput />
        </ManagedField>
      </ManagedForm>
    );
  }
}
