export default function workflow(state = { sections: [], status: {} }, action) {
  switch (action.type) {
    case 'WORKFLOW_SECTIONS_GET_RECEIVE':
      return Object.assign({}, state, {
        sections: action.sections || []
      });
    case 'WORKFLOW_STATUS_GET_RECEIVE':
      return Object.assign({}, state, {
        status: action.status || {}
      });
    default:
      return state;
  }
}
