import YoutubeApi from '../../services/YoutubeApi';

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
    return YoutubeApi.getCategories()
      .then(res => {
        dispatch(receiveCategories(res))
      })
      .catch(error => {
        dispatch(errorReceivingCategories(error))
      });
  };
}
