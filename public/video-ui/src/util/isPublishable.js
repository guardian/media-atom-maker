import { previewPrivacyState } from '../constants/privacyStates';

export function isPublishable(video) {

  let publishState = {
    errors: []
  };

  if (previewPrivacyState === video.privacyStatus) {
    publishState.errors.push('privacyStatus');
  }

  return publishState;
}

