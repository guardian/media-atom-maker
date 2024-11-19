import WorkflowApi from '../../services/WorkflowApi';
import { defaultWorkflowStatusData } from '../../constants/defaultWorkflowStatusData';

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
    status: defaultWorkflowStatusData()
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
      .catch(error => {
        if (error.status !== 404) {
          return dispatch(errorReceivingStatus(error));
        }
        try {
          error.json().then(errorBody => {
            if (errorBody.errors && errorBody.errors.message === 'ContentNotFound') {
              return dispatch(receiveStatus404());
            }
          });
        } catch (e) {
          // failed to parse response as json
          return dispatch(errorReceivingStatus(err));
        }
      });
  };
}
