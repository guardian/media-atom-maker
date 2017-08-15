import WorkflowApi from '../../services/WorkflowApi';

function requestSections() {
  return {
    type: 'WORKFLOW_SECTIONS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveSections(sections) {
  return {
    type: 'WORKFLOW_SECTIONS_GET_RECEIVE',
    receivedAt: Date.now(),
    sections: sections
  };
}

function errorReceivingSections(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get Workflow sections',
    error: error,
    receivedAt: Date.now()
  };
}

export function getSections() {
  return dispatch => {
    dispatch(requestSections());
    return WorkflowApi.getSections()
      .then(res => dispatch(receiveSections(res)))
      .catch(err => dispatch(errorReceivingSections(err)));
  };
}
