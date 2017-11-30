export default function targeting(state = { targets: [] }, action) {
  switch (action.type) {
    case 'TARGETING_GET_RECEIVE':
      return Object.assign({}, state, {
        targets: action.targets || []
      });
    default:
      return state;
  }
}
