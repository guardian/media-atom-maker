export default function asset(state = null, action) {
  switch (action.type) {
    case 'AUDITS_GET_RECEIVE':
      return action.audits || false;

    default:
      return state;
  }
}
