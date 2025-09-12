import { browserHistory } from 'react-router';
import VideosApi, { Video } from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { AppDispatch } from '../../util/setupStore';

export function deleteVideo(video: Video) {
  return (dispatch: AppDispatch) => {
    return VideosApi.deleteVideo(video.id)
      .then(() => {
        browserHistory.push('/videos');
      })
      .catch(error => {
        dispatch(showError(`Could not delete atom. ${error}`, error.response));
      });
  };
}
