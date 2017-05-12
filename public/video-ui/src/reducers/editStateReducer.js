export default function video(state = false, action) {
  switch (action.type) {
    case 'VIDEO_EDIT_STATE_REQUEST':
      return action.editState || false;
    default:
      return state;
  }
}
