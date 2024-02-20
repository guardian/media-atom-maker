import { frontPageSize } from '../constants/frontPageSize';
import {videoStore} from "../util/videoStore";

export default function videos(
  state = { entries: [], total: 0, page: frontPageSize },
  action
) {
  switch (action.type) {
    case 'VIDEOS_GET_REQUEST':
      return {
        entries: state.entries,
        total: state.total,
        page: action.page,
        shouldUseCreatedDateForSort: action.shouldUseCreatedDateForSort
      };
    case 'VIDEOS_GET_RECEIVE':
      return {
        entries: videoStore.addVideos(action.videos || []),
        total: action.total,
        page: state.page
      };
    default:
      return state;
  }
}
