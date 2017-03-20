import {UploadsApi} from '../../services/UploadsApi';

function runningUploads(uploads) {
  return {
    type: 'RUNNING_UPLOADS',
    receivedAt: Date.now(),
    uploads: uploads
  };
}

export function getUploads(atomId) {
    return dispatch => {
        UploadsApi.getUploads(atomId).then((uploads) => {
            dispatch(runningUploads(uploads));
        });
    };
}