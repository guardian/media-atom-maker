export default function error(state = false, action) {
  switch (action.type) {
    case 'CLEAR_ERROR':
      return false;

    case 'SHOW_ERROR':
      return action.message;

    default:
      return state;
  }
}
