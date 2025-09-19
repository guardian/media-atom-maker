import VideosApi from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { updateVideoUsageWebTitle } from '../../slices/usage';


function requestVideoPageUpdate() {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

export function updateVideoPage(video, usages, updatesTo) {

  return dispatch => {
    dispatch(requestVideoPageUpdate());

    return VideosApi.updateCanonicalPages(
      video,
      usages,
      updatesTo
    )
      .then(() => dispatch(updateVideoUsageWebTitle(video.title)))
      .catch(error => {
        const unknownError = 'An unknown error occurred. Please contact the Developers';

        try {
          const errorJson = JSON.parse(error.response);
          const errorKey = errorJson && errorJson.errorKey;

          const message = errorKey === 'insufficient-permission'
            ? `Could not update a Composer video page. You do not have sufficient Composer permissions (most likely <code>sensitivity_controls</code>). Please contact Central Production`
            : unknownError;

          dispatch(showError(message, error));
        } catch (e) {
          dispatch(showError(message, error));
        }
      });
  };
}
