import VideosApi from '../../services/VideosApi';
import Q from 'q';

function requestVideoUsages(id) {
  return {
    type:       'VIDEO_USAGE_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoUsages(usages, composerIdsWithUsage, id) {

  const usageObject = {[id]: {usagesWithoutComposer: usages, composerIdsWithUsage: composerIdsWithUsage}};

  return {
    type:           'VIDEO_USAGE_GET_RECEIVE',
    usages:         usageObject,
    receivedAt:     Date.now()
  };
}

function errorReceivingVideoUsages(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not get video usages',
    error:      error,
    receivedAt: Date.now()
  };
}

export function getUsages(id) {
  return dispatch => {
    dispatch(requestVideoUsages());
    return VideosApi.getVideoUsages(id)
    .then(res => {
      const usages = res.response.results;
      Q.all(usages.map(VideosApi.fetchComposerId))
      .then((composerIds) => {
        const composerIdsWithUsage = composerIds.reduce((idsWithUsage, composerId, index) => {
          if (composerId !== '') {
            idsWithUsage.push({usage: usages[index], composerId: composerId});
          }
          return idsWithUsage;
        }, []);
        const usagesWithoutComposer = usages.filter(usage => {
          return composerIdsWithUsage.every(idWithUsage => {
            return idWithUsage.usage !== usage;
          });
        });

        dispatch(receiveVideoUsages(usagesWithoutComposer, composerIdsWithUsage, id))
      });
    })
    .catch(error => {
      dispatch(errorReceivingVideoUsages(error))
    })
  };
}
