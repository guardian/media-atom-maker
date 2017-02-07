import { previewPrivacyState } from '../constants/privacyStates';

export function getPublishErrors(video) {

  const publishState = {
    errors: []
  };

  if (previewPrivacyState === video.privacyStatus) {
    publishState.errors.push('privacyStatus');
  }

  return publishState;
}

