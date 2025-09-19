import VideosApi, { Video } from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { fetchUsages, updateVideoUsageWebTitle, UsageData } from '../../slices/usage';
import { setSaving } from '../../slices/saveState';
import { setVideo } from '../../slices/video';
import { Action, ThunkDispatch } from '@reduxjs/toolkit';
import { RootState } from '../../util/setupStore';



export function saveVideo(video: Video) {
  return (dispatch: ThunkDispatch<RootState, void, Action>) => {
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
      });
  };
}
