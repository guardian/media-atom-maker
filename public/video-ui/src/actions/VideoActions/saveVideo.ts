import VideosApi, { Video } from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { setSaving } from '../../slices/saveState';
import { fetchUsages, updateVideoUsageWebTitle, UsageData } from '../../slices/usage';
import { setVideo } from '../../slices/video';
import { AppDispatch } from '../../util/setupStore';



export function saveVideo(video: Video) {
  return (dispatch: AppDispatch) => {
    dispatch(setSaving(true));
    dispatch(setVideo(video));
    return VideosApi.saveVideo(video.id, video)
      .then(res => {
        return dispatch(fetchUsages(video.id))
          .then(fetchUsagesAction => {
            if (fetchUsagesAction.meta.requestStatus === 'rejected') {
              throw new Error('fetchUsagesFulfilledAction rejected');
            }
            return VideosApi.updateCanonicalPages(video, fetchUsagesAction.payload as UsageData, 'preview');
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
      })
      .finally(() => {
        dispatch(setSaving(false));
      });
  };
}
