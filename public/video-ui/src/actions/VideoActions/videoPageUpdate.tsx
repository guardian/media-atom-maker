import VideosApi from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { updateVideoUsageWebTitle, UsageData } from '../../slices/usage';
import { AppDispatch } from '../../util/setupStore';

function requestVideoPageUpdate() {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

const UNKNOWN_ERROR =
  'An unknown error occurred. Please contact the Developers';

export function updateVideoPage(
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  video: any,
  usages: UsageData,
  updatesTo: string
) {
  return (dispatch: AppDispatch) => {
    dispatch(requestVideoPageUpdate());

    // @ts-expect-error TS(2345): Argument of type 'string' is not assignable to par... Remove this comment to see the full error message
    return VideosApi.updateCanonicalPages(video, usages, updatesTo)
      .then(() => dispatch(updateVideoUsageWebTitle(video.title)))
      .catch(error => {
        try {
          const errorJson = JSON.parse(error.response);
          const errorKey = errorJson && errorJson.errorKey;

          const message =
            errorKey === 'insufficient-permission'
              ? `Could not update a Composer video page. You do not have sufficient Composer permissions (most likely <code>sensitivity_controls</code>). Please contact Central Production`
              : UNKNOWN_ERROR;
          dispatch(showError(message, error));
        } catch (e) {
          dispatch(showError(UNKNOWN_ERROR, error));
        }
      });
  };
}
