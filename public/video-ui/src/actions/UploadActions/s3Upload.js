import { createUpload, uploadParts, uploadPacFile, uploadSubtitleFile, deleteSubtitleFile} from '../../services/UploadsApi';
import { errorDetails } from '../../util/errorDetails';

function uploadStarted(upload) {
  return {
    type: 'UPLOAD_STARTED',
    receivedAt: Date.now(),
    upload: upload
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
    message: error,
    error: error,
    receivedAt: Date.now()
  };
}

export function startVideoUpload({id, file, selfHost}) {
  return dispatch => {
    createUpload(id, file, selfHost).then(upload => {
      dispatch(uploadStarted(upload));

      const progress = completed => dispatch(uploadProgress(completed));

      return uploadParts(upload, upload.parts, file, progress)
        .then(() => {
          dispatch(uploadComplete());
        })
        .catch(err => {
          dispatch(uploadError(errorDetails(err)));
        });
    });
  };
}

export function startPacFileUpload({id, file}) {
  return dispatch => {
    return uploadPacFile({id, file}).then(() => {
      dispatch(uploadComplete());
    }).catch(err => {
      dispatch(uploadError(errorDetails(err)));
    });
  };
}

export function startSubtitleFileUpload({id, version, file}) {
  return dispatch => {
    return uploadSubtitleFile({id, version, file}).then(() => {
      dispatch(uploadComplete());
    }).catch(err => {
      dispatch(uploadError(errorDetails(err)));
    });
  };
}

export function deleteSubtitle({id, version}) {
  return dispatch => {
    return deleteSubtitleFile({id, version}).then(() => {
      dispatch(uploadComplete());
    }).catch(err => {
      dispatch(uploadError(errorDetails(err)));
    });
  };
}

export function resetS3Upload() {
  return { type: 'UPLOAD_RESET' };
}
