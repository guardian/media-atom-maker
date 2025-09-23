import VideosApi from '../../services/VideosApi';
import { setPublishedVideo } from '../../slices/video';
import { showError } from '../../slices/error';
import { AppDispatch } from '../../util/setupStore';

export function getPublishedVideo(id: string) {
  return (dispatch: AppDispatch) => {
    return VideosApi.fetchPublishedVideo(id)
      .then(video => {
        dispatch(setPublishedVideo(video));
      })
      .catch(error => {
        dispatch(showError('Could not get published video', error));
      });
  };
}
