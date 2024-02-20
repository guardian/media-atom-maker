import {videoStore} from "../../util/videoStore";

export function updateSearchTerm(searchTerm) {
  videoStore.clearVideos();
  return {
    type: 'UPDATE_SEARCH_TERM',
    searchTerm: searchTerm,
    receivedAt: Date.now()
  };
}
