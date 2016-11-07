export default function video(state = null, action) {
  switch (action.type) {

    case 'VIDEO_GET_RECEIVE':
      return action.video || false;

    case 'VIDEO_CREATE_RECEIVE':
      return action.video || false;

    case 'VIDEO_UPDATE_REQUEST':
      return action.video;

    case 'VIDEO_POPULATE_BLANK':
      return action.video;

    default:
      return state;
  }
}
