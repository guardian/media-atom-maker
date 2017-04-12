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

function uploadError(error) {
  return {
    type: 'SHOW_ERROR',
    message: `Error uploading video ${error}`,
    error: error,
    receivedAt: Date.now()
  };
}

export function startUpload(id, file, completeFn, selfHost) {
  return dispatch => {
    // Start prompting the user about reloading the page
    window.onbeforeunload = () => { return false; };

    UploadsApi.createUpload(id, file, selfHost).then((upload) => {
      const progress = (completed) => dispatch(uploadProgress(completed));

      const err = (err) => {
        window.onbeforeunload = undefined;
        dispatch(uploadError(err));
      };

      const complete = () => {
        // Stop prompting the user. The upload continues server-side
        window.onbeforeunload = undefined;

        dispatch(uploadComplete());
        completeFn();
      };

      const handle = new UploadHandle(upload, file, progress, complete, err);
      handle.start();

      dispatch(uploadStarted(upload));
    }).catch((err) => {
      window.onbeforeunload = undefined;
      dispatch(uploadError(err));
    });
  };
}
