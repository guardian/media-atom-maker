import VideosApi from '../../services/VideosApi';
import ContentApi from '../../services/capi';
import { showError } from '../../slices/error';
import { addNewlyCreatedVideoUsage } from '../../slices/usage';


function requestVideoPageCreate() {
  return {
    type: 'VIDEO_PAGE_CREATE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

export function createVideoPage(id, video, isTrainingMode) {
  return dispatch => {
    dispatch(requestVideoPageCreate());

    return VideosApi.createComposerPage(id, video)
      .then(res => {
        const composerId = res.data.id;
        const pagePath = res.data.identifiers.path.data;

        const addVideo = VideosApi.addVideoToComposerPage({
          composerId,
          video
        });

        const postCreation = isTrainingMode
          ? [VideosApi.preventPublication(composerId), addVideo]
          : [addVideo];

        return Promise.all(postCreation).then(() => {
          // it takes a little time for the new Composer page to get to CAPI,
          // so keep trying until success or timeout
          return ContentApi.getByPath(pagePath, true).then(capiResponse => {
            return dispatch(
              addNewlyCreatedVideoUsage(capiResponse.response.content)
            );
          });
        });
      })
      .catch(error => {
        dispatch(showError('Could not create a video page', error));
      });
  };
}
