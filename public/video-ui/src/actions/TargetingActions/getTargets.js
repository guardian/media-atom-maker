import TargetingApi from '../../services/TargetingApi';

function requestGetTargets() {
  return {
    type: 'TARGETING_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveGetTarget(targets) {
  return {
    type: 'TARGETING_GET_RECEIVE',
    receivedAt: Date.now(),
    targets: targets
  };
}

function errorGetTarget(error) {
  return {
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to get Target',
    error: error
  };
}

export function getTargets(video) {
  return dispatch => {
    dispatch(requestGetTargets());
    return TargetingApi.getTargets(video)
      .then(res => dispatch(receiveGetTarget(res)))
      .catch(err => dispatch(errorGetTarget(err)));
  };
}
