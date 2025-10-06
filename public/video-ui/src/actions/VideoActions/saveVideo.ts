import VideosApi, { Video } from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { fetchUsages, updateVideoUsageWebTitle, UsageData } from '../../slices/usage';
import {setVideo, setVideoSaveState} from '../../slices/video';
import { AppDispatch } from '../../util/setupStore';

const errorMessages = {
  saveVideoDefault: 'Could not save video',
  saveVideoConflict: 'Could not save video as another user (or tab) is currently editing this video',
  fetchUsagesRejected: 'Could not fetch usages to update canonical page',
  canonicalPageUpdate: 'Could not update canonical page'
};

export const saveVideo = (video: Video) => async (dispatch: AppDispatch) => {
  dispatch(setVideoSaveState(true));
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
    dispatch(setVideoSaveState(false));
    return;
  }
  dispatch(setVideo(savedVideo));

  const usageData: UsageData | undefined = await dispatch(fetchUsages(video.id)).unwrap().catch(error => {
    dispatch(showError(errorMessages.fetchUsagesRejected, error));
    return undefined;
  });
  if (!usageData) {
    dispatch(setVideoSaveState(false));
    return;
  }

  const canonicalPageUpdate: unknown[] | undefined = await VideosApi.updateCanonicalPages(
    video, usageData, 'preview'
  )
    .catch(error => {
      dispatch(showError(errorMessages.canonicalPageUpdate, error));
      return undefined;
    });
  if (!canonicalPageUpdate) {
    dispatch(setVideoSaveState(false));
    return;
  }

  dispatch(updateVideoUsageWebTitle(video.title));
  dispatch(setVideoSaveState(false));
};

