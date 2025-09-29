import TargetingApi from '../../services/TargetingApi';
import debounce from 'lodash/debounce';
import {requestUpdateTarget} from "../../slices/targeting";
import {showError} from "../../slices/error";

const debouncedUpdate = debounce(
  (dispatch, target) =>
    TargetingApi.updateTarget(target)
      .catch(err => dispatch(showError(`Failed to update Target`, err))),
  500
);

export function updateTarget(target) {
  return dispatch => {
    dispatch(requestUpdateTarget(target));
    return debouncedUpdate(dispatch, target);
  };
}
