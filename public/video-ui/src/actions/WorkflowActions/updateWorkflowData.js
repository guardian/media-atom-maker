import WorkflowApi from '../../services/WorkflowApi';

function requestDataUpdate() {
  return {
    type: 'WORKFLOW_DATA_PUT_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveDataUpdate(data) {
  return {
    type: 'WORKFLOW_DATA_PUT_RECEIVE',
    receivedAt: Date.now,
    data
  };
}

function errorUpdatingData(error) {
  return {
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to update Atom data in Workflow',
    error: error
  };
}

export function updateWorkflowData({ workflowItem }) {
  return dispatch => {
    dispatch(requestDataUpdate());
    return Promise.all([
      WorkflowApi.updateStatus(workflowItem),
      WorkflowApi.updateNote(workflowItem),
      WorkflowApi.updatePriority(workflowItem),
      WorkflowApi.updateProdOffice(workflowItem)
    ])
      .then(response => dispatch(receiveDataUpdate(response)))
      .catch(err => dispatch(errorUpdatingData(err)));
  };
}
