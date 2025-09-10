import { browserHistory } from 'react-router';
import VideosApi, { Video } from '../../services/VideosApi';
import { showError } from "../../slices/error";
import { AppDispatch } from "../../util/setupStore";

function requestVideoCreate() {
  return {
    type: 'VIDEO_CREATE_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoCreate(video: Video) {
  browserHistory.push('/videos/' + video.id);
  return {
    type: 'VIDEO_CREATE_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}

export function createVideo(video: Video) {
  return (dispatch: AppDispatch) => {
    dispatch(requestVideoCreate());
    return VideosApi.createVideo(video)
      .then(res => dispatch(receiveVideoCreate(res)))
      .catch(error => dispatch(showError('Could not create video', error)));
  };
}
