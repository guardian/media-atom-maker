export default function youtube(state = {categories: []}, action) {
  switch (action.type) {
    case 'YT_CATEGORIES_GET_RECEIVE':
      return Object.assign({}, state, {
        categories: action.categories || []
      });
    default:
      return state;
  }
}
