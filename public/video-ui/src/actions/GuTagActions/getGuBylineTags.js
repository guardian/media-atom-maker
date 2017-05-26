import ContentApi from '../../services/capi.js';

function receiveBylineTags(bylinesTags) {
  return {
    type: 'GU_BYLINE_TAGS_GET_RECEIVE',
    bylineTags: bylinesTags,
    receivedAt: Date.now()
  };
}

function errorReceivingBylineTags(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get Guardian byline tags',
    error: error,
    receivedAt: Date.now()
  };
}

export function getGuBylineTags() {
  return dispatch => {
    return ContentApi.getBylineTags()
      .then(capiResponse => {
        const bylineTags = capiResponse.response.results.map(result => {
          const tags = { id: result.id, webTitle: result.webTitle };
          return tags;
        });
        dispatch(receiveBylineTags(bylineTags));
      })
      .catch(error => {
        dispatch(errorReceivingBylineTags(error));
      });
  };
}
