import TargetingApi from '../../services/TargetingApi';

function requestDeleteTarget(target) {
  return {
    type: 'TARGETING_DELETE_REQUEST',
    receivedAt: Date.now(),
    target
  };
}

function receiveDeleteTarget(target) {
  return {
    type: 'TARGETING_DELETE_RECEIVE',
    receivedAt: Date.now(),
    target
  };
}

function errorDeleteTarget(target) {
  return {
    type: 'TARGETING_DELETE_FAILURE',
    receivedAt: Date.now(),
    target
  };
}

function showDeleteTargetError(error) {
  return {
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to delete Target',
    error: error
  };
}

export function deleteTarget(target) {
  return dispatch => {
    dispatch(requestDeleteTarget(target));
    return TargetingApi.deleteTarget(target)
      .then(() => dispatch(receiveDeleteTarget(target)))
      .catch(err => {
        dispatch(errorDeleteTarget(target));
        dispatch(showDeleteTargetError(err));
      });
  };
}
