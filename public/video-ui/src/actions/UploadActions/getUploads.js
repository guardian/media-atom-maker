import * as UploadsApi from '../../services/UploadsApi';
import { errorDetails } from '../../util/errorDetails';
import {setUploads} from "../../slices/uploads";
import {showError} from "../../slices/error";

export function getUploads(atomId) {
  return dispatch => {
    UploadsApi.getUploads(atomId)
      .then(uploads => {
        dispatch(setUploads(uploads));
      })
      .catch(error => {
        dispatch(showError(errorDetails(error), error));
      });
  };
}
