import { createUpload, uploadParts, uploadPacFile, uploadSubtitleFile, deleteSubtitleFile} from '../../services/UploadsApi';
import { errorDetails } from '../../util/errorDetails';
import {setS3UploadStatusToComplete, s3UploadProgress, setS3UploadStatusToError, s3UploadStarted} from "../../slices/s3Upload";
import {showError} from "../../slices/error";
import {uploadStarted} from "../../slices/uploads";

export function startVideoUpload({id, file, selfHost}) {
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

export function startPacFileUpload({id, file}) {
  return dispatch => {
    return uploadPacFile({id, file}).then(() => {
      dispatch(setS3UploadStatusToComplete());
    }).catch(err => {
      dispatch(showError(errorDetails(err), err));
      dispatch(setS3UploadStatusToError());
    });
  };
}

export function startSubtitleFileUpload({id, version, file}) {
  return dispatch => {
    return uploadSubtitleFile({id, version, file}).then(() => {
      dispatch(setS3UploadStatusToComplete());
    }).catch(err => {
      dispatch(showError(errorDetails(err), err));
      dispatch(setS3UploadStatusToError());
    });
  };
}

export function deleteSubtitle({id, version}) {
  return dispatch => {
    return deleteSubtitleFile({id, version}).then(() => {
      dispatch(setS3UploadStatusToComplete());
    }).catch(err => {
      dispatch(showError(errorDetails(err), err));
      dispatch(setS3UploadStatusToError());
    });
  };
}
