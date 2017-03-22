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

function uploadComplete() {
  return {
    type: 'UPLOAD_COMPLETE',
    receivedAt: Date.now()
  };
}

export function startUpload(id, file, completeFn) {
  return dispatch => {
    UploadsApi.createUpload(id, file).then((upload) => {
      const progress = (completed) => dispatch(uploadProgress(completed));
      const complete = () => {
        dispatch(uploadComplete());
        completeFn();
      };
      
      const handle = new UploadHandle(upload, file, progress, complete);
      handle.start();

      dispatch(uploadStarted(upload));
    });
  };
}