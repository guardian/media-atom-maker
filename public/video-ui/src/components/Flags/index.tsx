import React from 'react';
import { ManagedField, ManagedForm } from '../ManagedForm';
import CheckBox from '../FormFields/CheckBox';
import VideoUtils from '../../util/video';
import { formNames } from '../../constants/formNames';
import { addOrDropBundlingTags } from '../../services/KeywordsApi';
import { Video } from '../../services/VideosApi';

type Props = {
  video: Video;
  editable: boolean;
  updateVideo: (...args: any[]) => any;
  updateErrors: (...args: any[]) => any;
  updateWarnings: (...args: any[]) => any;
};

class Flags extends React.Component<Props> {
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

    const isCommercialType = VideoUtils.isCommercialType(video);
    const isEligibleForAds = VideoUtils.isEligibleForAds(video);
    const canHaveComposerPage = VideoUtils.canHaveComposerPage(video);
    const platform = VideoUtils.getPlatformFromAtom(video);

    return (
      // @ts-expect-error TS(2769): No overload matches this call.
      <ManagedForm
        data={video}
        updateData={updateVideo}
        editable={editable}
        updateErrors={updateErrors}
        updateWarnings={updateWarnings}
        formName={formNames.flags}
        formClass="atom__edit__form"
      >
        {canHaveComposerPage && (
          // @ts-expect-error TS(2769): No overload matches this call.
          <ManagedField
            fieldLocation="blockAds"
            name="Block ads"
            fieldDetails={
              isCommercialType
                ? 'Block ads on Composer page'
                : 'Ads will not be displayed with this video'
            }
            updateSideEffects={this.composerKeywordsToYouTube}
            disabled={platform !== 'youtube' || !isEligibleForAds}
            tooltip={!isEligibleForAds ? `Not eligible for pre-roll.` : ''}
          >
            {/* use a different field identifier to `fieldLocation` to ensure ad blockers don't remove it from the DOM */}
            {/* @ts-expect-error TS(2769): No overload matches this call. */}
            <CheckBox fieldId="what-a-time-to-be-alive" />
          </ManagedField>
        )}
        {canHaveComposerPage && (
          // @ts-expect-error TS(2769): No overload matches this call.
          <ManagedField
            fieldLocation="composerCommentsEnabled"
            name="Comments"
            fieldDetails="Allow comments on Guardian video page (does not change YouTube)"
          >
            {/* @ts-expect-error TS(2769): No overload matches this call. */}
            <CheckBox />
          </ManagedField>
        )}
        {canHaveComposerPage && (
          // @ts-expect-error TS(2769): No overload matches this call.
          <ManagedField
            fieldLocation="optimisedForWeb"
            name="Optimised for Web"
            fieldDetails="Optimised for Web"
          >
            {/* @ts-expect-error TS(2769): No overload matches this call. */}
            <CheckBox />
          </ManagedField>
        )}
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="sensitive"
          name="Sensitive"
          fieldDetails="Contains sensitive content"
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <CheckBox />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="legallySensitive"
          name="Legally Sensitive"
          fieldDetails="This content involves active criminal proceedings"
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <CheckBox />
        </ManagedField>
        {canHaveComposerPage && (
          // @ts-expect-error TS(2769): No overload matches this call.
          <ManagedField
            fieldLocation="suppressRelatedContent"
            name="Suppress related content"
            fieldDetails="Suppress related content"
          >
            {/* @ts-expect-error TS(2769): No overload matches this call. */}
            <CheckBox />
          </ManagedField>
        )}
      </ManagedForm>
    );
  }
}

export default Flags;
