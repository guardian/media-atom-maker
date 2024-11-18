export default function warnings(state = {}, action) {
  switch (action.type) {
    case 'FIELD_WARNINGS_UPDATE_REQUEST':
      return Object.assign({}, state, action.warning);

    default:
      return state;
  }
}
