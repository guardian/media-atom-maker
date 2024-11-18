import WorkflowApi from '../../services/WorkflowApi';

function receivePriorities(priorities) {
  return {
    type: 'WORKFLOW_PRIORITIES_GET_RECEIVE',
    receivedAt: Date.now(),
    priorities
  };
}

function errorReceivingPriorities(error) {
  return {
    type: 'SHOW_ERROR',
    message: `Could not get Workflow priorities. <a href="${WorkflowApi.workflowUrl}" target="_blank" rel="noopener">Open Workflow to get a cookie.</a>`,
    error: error,
    receivedAt: Date.now()
  };
}

export function getPriorities() {
  return dispatch => {
    return WorkflowApi.getPriorities()
      .then(res => dispatch(receivePriorities(res)))
      .catch(err => dispatch(errorReceivingPriorities(err)));
  };
}
