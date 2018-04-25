import WorkflowApi from '../../services/WorkflowApi';

function requestStatusUpdate() {
  return {
    type: 'WORKFLOW_STATUS_PUT_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveStatusUpdate(data) {
  return {
    type: 'WORKFLOW_STATUS_PUT_RECEIVE',
    receivedAt: Date.now,
    data
  };
}

function errorUpdatingStatus(error) {
  return {
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to update Atom status in Workflow',
    error: error
  };
}

export function updateWorkflowStatus({ workflowItem }) {
  return dispatch => {
    dispatch(requestStatusUpdate());
    return Promise.all([
      WorkflowApi.updateStatus(workflowItem),
      WorkflowApi.updateNote(workflowItem)
    ])
      .then(response => dispatch(receiveStatusUpdate(response)))
      .catch(err => dispatch(errorUpdatingStatus(err)));
  };
}
