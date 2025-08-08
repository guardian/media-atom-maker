export default function errorKeyState(
  state = 0,
  action
) {
  switch (action.type) {
    case 'SHOW_ERROR':
      return state + 1;

    default:
      return state;
  }
}
