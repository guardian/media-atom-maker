import TargetingApi from '../../services/TargetingApi';

function requestUpdateTarget() {
  return {
    type: 'TARGETING_UPDATE_REQUEST',
    receivedAt: Date.now()
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

export function updateTarget(target) {
  return dispatch => {
    dispatch(requestUpdateTarget());
    return TargetingApi.updateTarget(target)
      .then(() => dispatch(receiveUpdateTarget()))
      .catch(err => dispatch(errorUpdateTarget(err)));
  };
}
