import WorkflowApi from '../../services/WorkflowApi';

function requestStatuses() {
  return {
    type: 'WORKFLOW_STATUSES_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveStatuses(statuses) {
  return {
    type: 'WORKFLOW_STATUSES_GET_RECEIVE',
    receivedAt: Date.now(),
    statuses
  };
}

function errorReceivingStatuses(error) {
  return {
    type: 'SHOW_ERROR',
    message: `Could not get Workflow statuses. <a href="${WorkflowApi.workflowUrl}" target="_blank" rel="noopener">Open Workflow to get a cookie.</a>`,
    error: error,
    receivedAt: Date.now()
  };
}

export function getStatuses() {
  return dispatch => {
    dispatch(requestStatuses());
    return WorkflowApi.getStatuses()
      .then(res => dispatch(receiveStatuses(res)))
      .catch(err => dispatch(errorReceivingStatuses(err)));
  };
}
