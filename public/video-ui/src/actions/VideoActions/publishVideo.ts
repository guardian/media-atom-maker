import VideosApi, {Video} from '../../services/VideosApi';
import { setPublishing } from "../../slices/saveState";
import { showError } from "../../slices/error";
import { AppDispatch } from "../../util/setupStore";

function receiveVideoPublish(video: Video) {
  return {
    type: 'VIDEO_PUBLISH_RECEIVE',
    publishedVideo: video,
    receivedAt: Date.now()
  };
}

export function publishVideo(videoId: string) {
  return (dispatch: AppDispatch) => {
    dispatch(setPublishing(true));
    return VideosApi.publishVideo(videoId)
      .then(res => {
        dispatch(setPublishing(false));
        dispatch(receiveVideoPublish(res))
      })
      .catch(error => dispatch(showError(error.responseText, error)));
  };
}
