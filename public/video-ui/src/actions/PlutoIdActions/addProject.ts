import { Dispatch } from 'redux';
import { KnownAction } from '../../actions';
import VideosApi, { Video } from '../../services/VideosApi';

function requestAddProject(): KnownAction {
  return {
    type: 'ADD_PROJECT_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveAddProject(video:Video): KnownAction {
  return {
    type: 'ADD_PROJECT_RECEIVE',
    video: video,
    receivedAt: Date.now()
  };
}

function errorReceivingAddProject(error: unknown): KnownAction {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not add video to pluto project',
    error: error,
    receivedAt: Date.now()
  };
}

// this function isn't currently being used and the module could be removed
// if there is no future work planned that would need it.
export function addProject(video: Video) {
  return (dispatch: Dispatch<KnownAction>) => {
    dispatch(requestAddProject());

    return VideosApi.saveVideo(video.id, video)
      .then(() => dispatch(receiveAddProject(video)))
      .catch(error => dispatch(errorReceivingAddProject(error)));
  };
}
