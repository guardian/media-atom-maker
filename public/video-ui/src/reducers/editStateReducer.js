export default function video(state = {
    metadataEditable: false,
    youtubeEditable: false,
    videoDataEditable: false
  }, action) {
  switch (action.type) {
    case 'VIDEO_EDIT_STATE_REQUEST':
      return Object.assign({}, action.editState) || state;
    default:
      return state;
  }
}
