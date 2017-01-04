import VideosApi from '../../services/VideosApi';
import {searchText} from '../../services/capi';

function requestVideos() {
  return {
    type:       'VIDEOS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideos(videos) {
  return {
    type:       'VIDEOS_GET_RECEIVE',
    videos:     videos,
    receivedAt: Date.now()
  };
}

function requestSearchVideos() {
  return {
    type:       'VIDEOS_SEARCH_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveSearchVideos(videos) {
  return {
    type:       'VIDEOS_SEARCH_RECEIVE',
    videos:     videos,
    receivedAt: Date.now()
  };
}

function errorReceivingVideos(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get videos',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getVideos() {
  return dispatch => {
    dispatch(requestVideos());
    return VideosApi.fetchVideos()
      .then(res => {
        dispatch(receiveVideos(res));
      })
      .catch(error => dispatch(errorReceivingVideos(error)));
  };
}

export function searchVideosWithQuery(query) {
  return dispatch => {
    dispatch(requestSearchVideos());
    return searchText(query)
      .then(res => {
        const capiAtoms = res.response.results;
        const atoms = capiAtoms.map((capiAtom) => {
          return {
            id: capiAtom.id,
            title: capiAtom.data.media.title,
            contentChangeDetails: capiAtom.contentChangeDetails,
            assets: capiAtom.data.media.assets
          }
        });
        dispatch(receiveSearchVideos(atoms));
      })
      .catch(error => dispatch(errorReceivingVideos(error)));
  };
}
