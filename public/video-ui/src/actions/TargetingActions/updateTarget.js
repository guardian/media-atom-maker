import TargetingApi from '../../services/TargetingApi';
import debounce from 'lodash/debounce';
import {requestUpdateTarget} from "../../slices/targeting";
import {showError} from "../../slices/error";

function receiveUpdateTarget() {
  return {
    type: 'TARGETING_UPDATE_RECEIVE',
    receivedAt: Date.now()
  };
}

const debouncedUpdate = debounce(
  (dispatch, target) =>
    TargetingApi.updateTarget(target)
      .then(() => dispatch(receiveUpdateTarget()))
      .catch(err => dispatch(showError(`Could not create asset.`, err))),
  500
);

export function updateTarget(target) {
  return dispatch => {
    dispatch(requestUpdateTarget(target));
    return debouncedUpdate(dispatch, target);
  };
}


