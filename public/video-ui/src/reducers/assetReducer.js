export default function asset(state = null, action) {
  switch (action.type) {

    case 'ASSET_CREATE_RECEIVE':
      return action.asset || false;

    case 'ASSET_UPDATE_REQUEST':
      return action.asset;

    case 'ASSET_POPULATE_BLANK':
      return action.asset;

    default:
      return state;
  }
}
