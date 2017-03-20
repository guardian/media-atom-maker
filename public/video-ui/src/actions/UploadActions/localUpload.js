import {UploadsApi, UploadHandle} from '../../services/UploadsApi';

function uploadStarted(upload, handle) {
  return {
    type: 'UPLOAD_STARTED',
    receivedAt: Date.now(),
    upload: upload,
    handle: handle
  };
}

function uploadProgress(progress) {
  return {
    type: 'UPLOAD_PROGRESS',
    receivedAt: Date.now(),
    progress: progress
  };
}

export function startUpload(id, file) {
  return dispatch => {
    UploadsApi.createUpload(id, file).then((upload) => {
      const progress = (completed) => dispatch(uploadProgress(completed));
      const handle = new UploadHandle(upload, file, progress);

      handle.start();
      dispatch(uploadStarted(upload));
    });
  };
}