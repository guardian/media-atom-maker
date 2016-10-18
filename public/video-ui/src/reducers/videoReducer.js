export default function video(state = null, action) {
  switch (action.type) {

    case 'VIDEO_GET_RECIEVE':
      return action.video || false;

    case 'VIDEO_UPDATE_REQUEST':
      return action.video;

    default:
      return state;
  }
}
