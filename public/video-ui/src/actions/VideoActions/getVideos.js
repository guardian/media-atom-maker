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
    dispatch(requestVideos());
    return searchText(query)
      .catch(error => dispatch(errorReceivingVideos(error)))
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
        dispatch(receiveVideos(atoms));
      });
  };
}
