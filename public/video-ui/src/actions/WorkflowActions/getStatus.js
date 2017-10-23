import WorkflowApi from '../../services/WorkflowApi';
import { Workflow as WorkflowConstants } from '../../constants/workflow';

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

function receiveStatus404() {
  return {
    type: 'WORKFLOW_STATUS_NOT_FOUND',
    receivedAt: Date.now(),
    status: WorkflowConstants.notInWorkflow
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

export function getStatus(video) {
  return dispatch => {
    dispatch(requestStatus());
    return WorkflowApi.getAtomInWorkflow(video)
      .then(res => dispatch(receiveStatus(res)))
      .catch(err => {
        if (err.status !== 404) {
          return dispatch(errorReceivingStatus(err));
        }

        try {
          const errJson = JSON.parse(err.response);

          if (errJson.errors && errJson.errors.message === 'ContentNotFound') {
            return dispatch(receiveStatus404());
          }
          return dispatch(errorReceivingStatus(err));
        } catch (e) {
          // failed to parse response as json
          return dispatch(errorReceivingStatus(err));
        }
      });
  };
}
