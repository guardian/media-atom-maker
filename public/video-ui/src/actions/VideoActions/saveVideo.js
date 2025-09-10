import VideosApi from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { setSaving } from '../../slices/saveState';
import { setVideo } from '../../slices/video';

function receiveVideoPageUpdate(newTitle) {
  return {
    type: 'VIDEO_PAGE_UPDATE_POST_RECEIVE',
    newTitle: newTitle,
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

export function saveVideo(video) {
  return dispatch => {
    dispatch(setSaving(true));
    dispatch(setVideo(video));
    return VideosApi.saveVideo(video.id, video)
      .then(res => {
        dispatch(setSaving(false));
        dispatch(setVideo(res));
        return VideosApi.getVideoUsages(video.id)
          .then(usages => {
            dispatch(receiveVideoUsages(usages));
            return VideosApi.updateCanonicalPages(video, usages, 'preview');
          })
          .then(() => dispatch(receiveVideoPageUpdate(video.title)))
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
