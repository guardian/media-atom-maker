export default function youtube(state = {categories: [], channels: []}, action) {
  switch (action.type) {
    case 'YT_CATEGORIES_GET_RECEIVE':
      return Object.assign({}, state, {
        categories: action.categories || []
      });
    case 'YT_CHANNELS_GET_RECEIVE':
      return Object.assign({}, state, {
        channels: action.channels
      });
    default:
      return state;
  }
}
