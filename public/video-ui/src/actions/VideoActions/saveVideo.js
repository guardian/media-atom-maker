import VideosApi from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { fetchUsages } from '../../slices/usage';

function requestVideoSave(video) {
  return {
    type: 'VIDEO_SAVE_REQUEST',
    video: video,
    receivedAt: Date.now()
  };
}

function receiveVideoPageUpdate(newTitle) {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_RECEIVE',
    newTitle: newTitle,
    receivedAt: Date.now()
  };
}

function receiveVideoSave(video) {
  return {
    type: 'VIDEO_SAVE_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}


export function saveVideo(video) {
  return dispatch => {
    dispatch(requestVideoSave(video));
    return VideosApi.saveVideo(video.id, video)
      .then(res => {
        dispatch(receiveVideoSave(res));
        return dispatch(fetchUsages(video.id))
          .then(fetchUsagesFulfilledAction => {
            return VideosApi.updateCanonicalPages(video, fetchUsagesFulfilledAction.payload, 'preview');
          })
          .then(() => {
            dispatch(receiveVideoPageUpdate(video.title));
          })
          .catch(error => () => {
            dispatch(showError('Could not update canonical page', error));
          });
      })
      .catch(error => {
        const defaultError = 'Could not save video';
        const conflictError =
          'Could not save video as another user (or tab) is currently editing this video';
        const message = error.status === 409 ? conflictError : defaultError;
        dispatch(showError(message, error));
        throw error;
      });
  };
}
