import VideosApi from '../../services/VideosApi';

function requestVideoPageCreate(id) {
  return {
    type:       'VIDEO_PAGE_POST_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveVideoPageCreate(video) {
  return {
    type:           'VIDEO_PAGE_POST_RECEIVE',
    video:          video,
    receivedAt:     Date.now()
  };
}

function errorReceivingVideoPageCreate(error) {
  return {
    type:       'SHOW_ERROR',
    message:    'Could not create a video page',
    error:      error,
    receivedAt: Date.now()
  };
}

export function createPage(id, title, data, video, composerUrl) {
  let pageId;
  return dispatch => {
    dispatch(requestVideoPageCreate());
    return VideosApi.createVideoPage(id, title, composerUrl)
    .then(res => {
      pageId = res.data.id;
      return VideosApi.addVideoToPage(pageId, data, composerUrl)
    })
    .then(() => {
      video.composerPageId = pageId;
      return VideosApi.saveVideo(id, video)
    })
    .then(res => {
      dispatch(receiveVideoPageCreate(video))
    })
    .catch(error => {
      dispatch(errorReceivingVideoPageCreate(error))
    })
  };
}
