export default function workflow(state = { sections: [] }, action) {
  switch (action.type) {
    case 'WORKFLOW_SECTIONS_GET_RECEIVE':
      return Object.assign({}, state, {
        sections: action.sections || []
      });
    default:
      return state;
  }
}
