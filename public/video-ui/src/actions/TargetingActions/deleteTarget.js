import TargetingApi from '../../services/TargetingApi';

function requestDeleteTarget() {
  return {
    type: 'TARGETING_DELETE_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveDeleteTarget(targets) {
  return {
    type: 'TARGETING_DELETE_RECEIVE',
    receivedAt: Date.now(),
    targets: targets
  };
}

function errorDeleteTarget(error) {
  return {
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to delete Target',
    error: error
  };
}

export function deleteTarget(video, target) {
  return dispatch => {
    dispatch(requestDeleteTarget());
    return TargetingApi.deleteTarget(video, target)
      .then(res => dispatch(receiveDeleteTarget(res)))
      .catch(err => dispatch(errorDeleteTarget(err)));
  }
}
