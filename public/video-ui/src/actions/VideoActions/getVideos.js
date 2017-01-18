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

function adaptCapiAtom(atom) {
  const ret = {
    id: atom.id,
    activeVersion: -1, // not known
    title: atom.data.media.title,
    contentChangeDetails: atom.contentChangeDetails,
    assets: atom.data.media.assets
  };

  if(atom.data.media.posterImage)
    ret.posterImage = atom.data.media.posterImage;

  return ret;
}

export function searchVideosWithQuery(query) {
  return dispatch => {
    dispatch(requestSearchVideos());

    return searchText(query)
      .then(res => {
        const capiAtoms = res.response.results;
        const atoms = capiAtoms.map(adaptCapiAtom);

        dispatch(receiveSearchVideos(atoms));
      })
      .catch(error => {
        dispatch(errorReceivingVideos(error))
      });
  };
}
