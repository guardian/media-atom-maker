import WorkflowApi from '../../services/WorkflowApi';

function requestStatus() {
  return {
    type: 'WORKFLOW_STATUS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveStatus(status) {
  return {
    type: 'WORKFLOW_STATUS_GET_RECEIVE',
    receivedAt: Date.now(),
    status: status
  };
}

function errorReceivingStatus(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Cannot get Atom status in Workflow',
    error: error,
    receivedAt: Date.now()
  };
}

export function getStatus(id) {
  return dispatch => {
    dispatch(requestStatus());
    return WorkflowApi.getAtomInWorkflow(id)
      .then(res => dispatch(receiveStatus(res)))
      .catch(err => dispatch(errorReceivingStatus(err)));
  };
}
