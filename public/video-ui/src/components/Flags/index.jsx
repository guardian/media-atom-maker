import React from 'react';
import PropTypes from 'prop-types';
import { ManagedField, ManagedForm } from '../ManagedForm';
import CheckBox from '../FormFields/CheckBox';
import VideoUtils from '../../util/video';
import { formNames } from '../../constants/formNames';

class Flags extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    editable: PropTypes.bool.isRequired,
    updateVideo: PropTypes.func.isRequired,
    updateErrors: PropTypes.func.isRequired,
    updateWarnings: PropTypes.func.isRequired
  };

  render() {
    const {
      video,
      editable,
      updateVideo,
      updateErrors,
      updateWarnings
    } = this.props;

    const isYoutubeAtom = VideoUtils.isYoutube(video);
    const isCommercialType = VideoUtils.isCommercialType(video);
    const isEligibleForAds = VideoUtils.isEligibleForAds(video);

    return (
      <ManagedForm
        data={video}
        updateData={updateVideo}
        editable={editable}
        updateErrors={updateErrors}
        updateWarnings={updateWarnings}
        formName={formNames.flags}
        formClass="atom__edit__form"
      >
        <ManagedField
          fieldLocation="isLoopingVideo"
          name="Looping video"
          fieldDetails="This video has been designed to auto-loop in environments which permit such behaviour"
          disabled={isYoutubeAtom || isEligibleForAds}
        >
          <CheckBox />
        </ManagedField>
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
          {/* use a different field identifier to `fieldLocation` to ensure ad blockers don't remove it from the DOM */}
          <CheckBox fieldId="what-a-time-to-be-alive"/>
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
    );
  }
}

export default Flags;
