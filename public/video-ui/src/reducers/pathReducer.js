export default function path(state = '', action) {
  switch (action.type) {
    case 'PATH_UPDATE':
      return action.path || state;
    default:
      return state;
  }
}
