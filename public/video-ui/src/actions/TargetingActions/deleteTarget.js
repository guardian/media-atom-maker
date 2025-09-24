import TargetingApi from '../../services/TargetingApi';
import {errorDeleteTarget, receiveDeleteTarget, requestDeleteTarget} from "../../slices/targeting";
import {showError} from "../../slices/error";

export function deleteTarget(target) {
  return dispatch => {
    dispatch(requestDeleteTarget(target));
    return TargetingApi.deleteTarget(target)
      .then(() => dispatch(receiveDeleteTarget(target)))
      .catch(err => {
        dispatch(errorDeleteTarget(target));
        dispatch(showError(`Failed to delete Target`, err));
      });
  };
}
