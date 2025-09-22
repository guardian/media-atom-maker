import VideosApi, { Video } from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { setSaving } from '../../slices/saveState';
import { fetchUsages, updateVideoUsageWebTitle, UsageData } from '../../slices/usage';
import { setVideo } from '../../slices/video';
import { AppDispatch } from '../../util/setupStore';


export const saveVideo = (video: Video) => async (dispatch: AppDispatch) => {

  dispatch(setSaving(true));
  dispatch(setVideo(video));

  const savedVideo: Video | undefined = await VideosApi.saveVideo(video.id, video)
    .catch(error => {
      const defaultError = 'Could not save video';
      const conflictError =
        'Could not save video as another user (or tab) is currently editing this video';
      const message = error.status === 409 ? conflictError : defaultError;
      dispatch(showError(message, error));
      return undefined;
    });
  if (!savedVideo) {
    dispatch(setSaving(false));
    return;
  }

  const fetchUsagesAction = await dispatch(fetchUsages(video.id));
  if (fetchUsagesAction.meta.requestStatus !== 'fulfilled') {
    dispatch(showError('Could not fetch usages to update canonical page', new Error(fetchUsagesAction.meta.requestStatus)));
    dispatch(setSaving(false));
    return;
  }


  const canonicalPageUpdate: unknown[] | undefined = await VideosApi.updateCanonicalPages(
    video, fetchUsagesAction.payload as UsageData,
    'preview'
  )
    .catch(error => {
      dispatch(showError('Could not update canonical page', error));
      return undefined;
    });
  if (!canonicalPageUpdate) {
    dispatch(setSaving(false));
    return;
  }

  dispatch(updateVideoUsageWebTitle(video.title));
  dispatch(setSaving(false));
};

