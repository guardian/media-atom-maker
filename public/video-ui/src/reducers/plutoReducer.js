export default function pluto(state = { commissions: [], projects: [] }, action) {
  switch (action.type) {
    case 'PLUTO_COMMISSIONS_GET_RECEIVE':
      return Object.assign({}, state, {
        commissions: action.commissions || []
      });
    case 'PLUTO_PROJECTS_GET_RECEIVE':
      return Object.assign({}, state, {
        projects: action.projects || []
      });
    default:
      return state;
  }
}
