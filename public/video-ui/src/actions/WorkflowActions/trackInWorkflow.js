import WorkflowApi from '../../services/WorkflowApi';

function requestTrackInWorkflow() {
  return {
    type: 'WORKFLOW_TRACK_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveTrackInWorkflow(data) {
  return {
    type: 'WORKFLOW_TRACK_POST_RECEIVE',
    receivedAt: Date.now,
    data
  };
}

function errorTrackInWorkflow(error) {
  return {
    type: 'SHOW_ERROR',
    receivedAt: Date.now(),
    message: 'Failed to track Atom in Workflow',
    error: error
  };
}

export function trackInWorkflow({ video, status, section, prodOffice }) {
  return dispatch => {
    dispatch(requestTrackInWorkflow());
    return WorkflowApi.trackInWorkflow({ video, status, section, prodOffice })
      .then(response => dispatch(receiveTrackInWorkflow(response)))
      .catch(err => dispatch(errorTrackInWorkflow(err)));
  };
}
