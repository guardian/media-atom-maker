import { UploadsApi } from '../../services/UploadsApi';
import { errorDetails } from '../../util/errorDetails';

function runningUploads(uploads) {
  return {
    type: 'RUNNING_UPLOADS',
    receivedAt: Date.now(),
    uploads: uploads
  };
}

function errorGettingUploads(error) {
  return {
    type: 'SHOW_ERROR',
    message: `Could not get uploads: ${error}`,
    error: error,
    receivedAt: Date.now()
  };
}

export function getUploads(atomId) {
  return dispatch => {
    UploadsApi.getUploads(atomId)
      .then(uploads => {
        dispatch(runningUploads(uploads));
      })
      .catch(error => {
        dispatch(errorGettingUploads(errorDetails(error)));
      });
  };
}
