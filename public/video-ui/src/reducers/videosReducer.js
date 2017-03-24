export default function videos(state = { entries: [], limit: 2 }, action) {
  switch (action.type) {
    case 'VIDEOS_GET_REQUEST':
      return { entries: state.entries, limit: action.limit };
    case 'VIDEOS_SEARCH_REQUEST':
      return { entries: state.entries, limit: -1 };
    case 'VIDEOS_GET_RECEIVE':
      return { entries: action.videos || [], limit: state.limit };
    case 'VIDEOS_SEARCH_RECEIVE':
      return { entries: action.videos || [], limit: state.limit };
    default:
      return state;
  }
}
