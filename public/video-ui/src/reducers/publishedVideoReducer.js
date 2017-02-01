export default function video(state = null, action) {
  switch (action.type) {

    case 'PUBLISHED_VIDEO_GET_RECEIVE':
      return action.publishedVideo || false;

    case 'VIDEO_PUBLISH_RECEIVE':
      return action.publishedVideo || false;

    default:
      return state;
  }
}
