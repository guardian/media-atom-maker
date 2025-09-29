import { browserHistory } from 'react-router';
import VideosApi, { Video } from '../../services/VideosApi';
import { showError } from '../../slices/error';
import { AppDispatch } from '../../util/setupStore';
import { setSaving } from '../../slices/saveState';
import { setVideo } from '../../slices/video';

export function createVideo(video: Video) {
  return (dispatch: AppDispatch) => {
    dispatch(setSaving(true));
    return VideosApi.createVideo(video)
      .then(res => {
        browserHistory.push('/videos/' + res.id);
        dispatch(setSaving(false));
        dispatch(setVideo(res));
      })
      .catch(error => {
        dispatch(showError('Could not create video', error));
        throw error;
      });
  };
}
