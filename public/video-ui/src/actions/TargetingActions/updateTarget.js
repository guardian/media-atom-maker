import TargetingApi from '../../services/TargetingApi';
import debounce from 'lodash/debounce';

function requestUpdateTarget(target) {
  return {
    type: 'TARGETING_UPDATE_REQUEST',
    receivedAt: Date.now(),
    target
  };
}

function receiveUpdateTarget() {
  return {
    type: 'TARGETING_UPDATE_RECEIVE',
    receivedAt: Date.now()
  };
}

function errorUpdateTarget(error) {
  return {
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to update Target',
    error: error
  };
}

const debouncedUpdate = debounce(
  (dispatch, target) =>
    TargetingApi.updateTarget(target)
      .then(() => dispatch(receiveUpdateTarget()))
      .catch(err => dispatch(errorUpdateTarget(err))),
  500
);

export function updateTarget(target) {
  return dispatch => {
    dispatch(requestUpdateTarget(target));
    return debouncedUpdate(dispatch, target);
  };
}
