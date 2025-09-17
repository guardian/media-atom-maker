export default function workflow(state = { sections: [], statuses: [], status: {}, priorities: [] }, action) {
  switch (action.type) {
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
