import {getYoutubeCategories} from '../../services/YoutubeApi';

function requestCategories() {
  return {
    type: 'YT_CATEGORIES_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveCategories(categories) {
  return {
    type: 'YT_CATEGORIES_GET_RECEIVE',
    categories: categories,
    receivedAt: Date.now()
  };
}

function errorReceivingCatetories(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube categories',
    error: error,
    receivedAt: Date.now()
  }
}

export function getCategories() {
  return dispatch => {
    dispatch(requestCategories());
    return getYoutubeCategories()
      .catch(error => dispatch(errorReceivingCatetories(error)))
      .then(categories => dispatch(receiveCategories(categories)));
  };
}
