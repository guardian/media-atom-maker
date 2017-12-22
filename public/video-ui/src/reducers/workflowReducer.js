export default function workflow(state = { sections: [], statuses: [], status: {} }, action) {
  switch (action.type) {
    case 'WORKFLOW_SECTIONS_GET_RECEIVE':
      return Object.assign({}, state, {
        sections: action.sections || []
      });
    case 'WORKFLOW_STATUSES_GET_RECEIVE':
      return Object.assign({}, state, {
        statuses: action.statuses || []
      });
    case 'WORKFLOW_STATUS_GET_RECEIVE':
      return Object.assign({}, state, {
        status: Object.assign({}, {isTrackedInWorkflow: true}, action.status) || {}
      });
    case 'WORKFLOW_STATUS_NOT_FOUND':
      return Object.assign({}, state, {
        status: Object.assign({}, {isTrackedInWorkflow: false}, action.status)
      });
    case 'WORKFLOW_VIDEO_UPDATE_REQUEST':
      return Object.assign({}, state, {
        status: action.status
      });
    default:
      return state;
  }
}
