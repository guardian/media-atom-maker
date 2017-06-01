export default function config(state = {}, action) {
  switch (action.type) {
    case 'CONFIG_RECEIVED':
      return action.config || {};

    case 'PRESENCE_STARTED':
      return Object.assign({}, state, { presence: action.client });

    default:
      return state;
  }
}
