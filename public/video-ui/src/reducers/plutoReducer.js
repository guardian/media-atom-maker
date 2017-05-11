export default function pluto(state = { projects: [] }, action) {
  switch (action.type) {
    case 'PLUTO_PROJECTS_GET_RECEIVE':
      return Object.assign({}, state, {
        projects: action.projects || []
      });
    default:
      return state;
  }
}
