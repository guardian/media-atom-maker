import VideosApi, { Video } from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { setSaving } from '../../slices/saveState';
import { fetchUsages, updateVideoUsageWebTitle, UsageData } from '../../slices/usage';
import { setVideo } from '../../slices/video';
import { AppDispatch } from '../../util/setupStore';

const errorMessages = {
  saveVideoDefault: 'Could not save video',
  saveVideoConflict: 'Could not save video as another user (or tab) is currently editing this video',
  fetchUsagesRejected: 'Could not fetch usages to update canonical page',
  canonicalPageUpdate: 'Could not update canonical page'
};

export const saveVideo = (video: Video) => async (dispatch: AppDispatch) => {

  dispatch(setSaving(true));
  dispatch(setVideo(video));

  const savedVideo: Video | undefined = await VideosApi.saveVideo(video.id, video)
    .catch(error => {
      const message = error.status === 409
        ? errorMessages.saveVideoConflict
        : errorMessages.saveVideoDefault;
      dispatch(showError(message, error));
      return undefined;
    });
  if (!savedVideo) {
    dispatch(setSaving(false));
    return;
  }

  const fetchUsagesAction = await dispatch(fetchUsages(video.id));
  if (fetchUsagesAction.meta.requestStatus !== 'fulfilled') {
    dispatch(showError(errorMessages.fetchUsagesRejected, new Error(errorMessages.fetchUsagesRejected)));
    dispatch(setSaving(false));
    return;
  }

  const canonicalPageUpdate: unknown[] | undefined = await VideosApi.updateCanonicalPages(
    video, fetchUsagesAction.payload as UsageData,
    'preview'
  )
    .catch(error => {
      dispatch(showError(errorMessages.canonicalPageUpdate, error));
      return undefined;
    });
  if (!canonicalPageUpdate) {
    dispatch(setSaving(false));
    return;
  }

  dispatch(updateVideoUsageWebTitle(video.title));
  dispatch(setSaving(false));
};

