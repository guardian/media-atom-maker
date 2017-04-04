import {frontPageSize} from '../constants/frontPageSize';

export default function videos(state = { entries: [], total: 0, limit: frontPageSize }, action) {
  switch (action.type) {
    case 'VIDEOS_GET_REQUEST':
      return { entries: state.entries, total: state.total, limit: action.limit };
    case 'VIDEOS_GET_RECEIVE':
      return { entries: action.videos || [], total: action.total, limit: state.limit };
    default:
      return state;
  }
}
