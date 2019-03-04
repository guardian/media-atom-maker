import React from 'react';
import { ManagedField, ManagedForm } from '../ManagedForm';
import CheckBox from '../FormFields/CheckBox';
import VideoUtils from '../../util/video';

class Flags extends React.Component {
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

    const isYoutubeAtom = VideoUtils.isYoutube(video);
    const isCommercialType = VideoUtils.isCommercialType(video);
    const isEligibleForAds = VideoUtils.isEligibleForAds(video);

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
          <ManagedField
            fieldLocation="blockAds"
            name="Block ads"
            fieldDetails={
              isCommercialType
                ? 'Block ads on Composer page'
                : 'Ads will not be displayed with this video'
            }
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
        </ManagedForm>
      </div>
    );
  }
}

export default Flags;
