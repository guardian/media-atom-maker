import TargetingApi from '../../services/TargetingApi';

function requestCreateTarget() {
  return {
    type: 'TARGETING_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveCreateTarget(targets) {
  return {
    type: 'TARGETING_POST_RECEIVE',
    receivedAt: Date.now(),
    targets: [targets]
  };
}

function errorCreateTarget(error) {
  return {
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to create Target',
    error: error
  };
}

export function createTarget(video) {
  return dispatch => {
    dispatch(requestCreateTarget());
    return TargetingApi.createTarget(video)
      .then(res => dispatch(receiveCreateTarget(res)))
      .catch(err => dispatch(errorCreateTarget(err)));
  };
}
