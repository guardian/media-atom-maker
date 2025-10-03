import {
  createUpload,
  deleteSubtitleFile,
  uploadPacFile,
  uploadParts,
  uploadSubtitleFile
} from '../../services/UploadsApi';
import { showError } from '../../slices/error';
import {
  s3UploadProgress,
  s3UploadStarted,
  setS3UploadStatusToComplete,
  setS3UploadStatusToError
} from '../../slices/s3Upload';
import { errorDetails } from '../../util/errorDetails';

export function startVideoUpload({ id, file, selfHost }) {
  return dispatch => {
    createUpload(id, file, selfHost).then(upload => {
      dispatch(s3UploadStarted(upload));

      const progress = completed => dispatch(s3UploadProgress(completed));

      return uploadParts(upload, upload.parts, file, progress)
        .then(() => {
          dispatch(setS3UploadStatusToComplete());
        })
        .catch(err => {
          dispatch(showError(errorDetails(err), err));
          dispatch(setS3UploadStatusToError());
        });
    });
  };
}

export function startPacFileUpload({ id, file }) {
  return dispatch => {
    return uploadPacFile({ id, file })
      .then(() => {
        dispatch(setS3UploadStatusToComplete());
      })
      .catch(err => {
        dispatch(showError(errorDetails(err), err));
        dispatch(setS3UploadStatusToError());
      });
  };
}

export function startSubtitleFileUpload({ id, version, file }) {
  return dispatch => {
    return uploadSubtitleFile({ id, version, file })
      .then(() => {
        dispatch(setS3UploadStatusToComplete());
      })
      .catch(err => {
        dispatch(showError(errorDetails(err), err));
        dispatch(setS3UploadStatusToError());
      });
  };
}

export function deleteSubtitle({ id, version }) {
  return dispatch => {
    return deleteSubtitleFile({ id, version })
      .then(() => {
        dispatch(setS3UploadStatusToComplete());
      })
      .catch(err => {
        dispatch(showError(errorDetails(err), err));
        dispatch(setS3UploadStatusToError());
      });
  };
}

export const s3UploadActions = {
  startVideoUpload,
  startPacFileUpload,
  startSubtitleFileUpload,
  deleteSubtitle
};
