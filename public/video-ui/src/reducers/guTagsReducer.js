export default function guTags(state = { bylineTags: [] }, action) {
  switch (action.type) {
    case 'GU_BYLINE_TAGS_GET_RECEIVE':
      return Object.assign({}, state, {
        bylineTags: action.bylineTags || []
      });
    default:
      return state;
  }
}
