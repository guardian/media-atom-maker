import VideosApi from '../../services/VideosApi';
import ContentApi from '../../services/capi';

function requestVideoUsages() {
  return {
    type: 'VIDEO_USAGE_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoUsages(usages) {
  return {
    type: 'VIDEO_USAGE_GET_RECEIVE',
    usages: usages,
    receivedAt: Date.now()
  };
}

function errorReceivingVideoUsages(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get video usages',
    error: error,
    receivedAt: Date.now()
  };
}

export function getUsages(id) {
  return dispatch => {
    dispatch(requestVideoUsages());

    if (!id) {
      return dispatch(receiveVideoUsages([]));
    }

    return VideosApi.getVideoUsages(id)
      .then(res => {
        const usagePaths = res.response.results;

        // the atom usage endpoint in capi only returns article paths,
        // lookup the articles in capi to get their fields
        Promise.all(usagePaths.map(ContentApi.getByPath)).then(capiResponse => {
          const usages = capiResponse.reduce((all, item) => {
            all.push(item.response.content);
            return all;
          }, []);

          // sort by article creation date DESC
          usages.sort(
            (first, second) =>
              new Date(second.fields.creationDate) -
              new Date(first.fields.creationDate)
          );

          dispatch(receiveVideoUsages(usages));
        });
      })
      .catch(error => {
        dispatch(errorReceivingVideoUsages(error));
      });
  };
}
