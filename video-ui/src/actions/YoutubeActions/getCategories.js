import { getYoutubeCategories } from '../../services/YoutubeApi';
import Logger from '../../logger';

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

function errorReceivingCategories(error) {
  Logger.error(error);
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube categories',
    error: error,
    receivedAt: Date.now()
  };
}

export function getCategories() {
  return dispatch => {
    dispatch(requestCategories());
    return getYoutubeCategories()
      .then(res => {
        dispatch(receiveCategories(res));
      })
      .catch(error => {
        dispatch(errorReceivingCategories(error));
      });
  };
}
