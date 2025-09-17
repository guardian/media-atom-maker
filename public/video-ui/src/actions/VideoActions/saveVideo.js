import VideosApi from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { fetchUsages, updateVideoUsageWebTitle } from '../../slices/usage';
import { setSaving } from '../../slices/saveState';
import { setVideo } from '../../slices/video';


export function saveVideo(video) {
  return dispatch => {
    dispatch(setSaving(true));
    dispatch(setVideo(video));
    return VideosApi.saveVideo(video.id, video)
      .then(res => {
        return dispatch(fetchUsages(video.id))
          .then(fetchUsagesFulfilledAction => {
            return VideosApi.updateCanonicalPages(video, fetchUsagesFulfilledAction.payload, 'preview');
          })
          .then(() => {
            dispatch(updateVideoUsageWebTitle(video.title));
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
