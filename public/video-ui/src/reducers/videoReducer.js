export default function video(state = null, action) {
  switch (action.type) {

    case 'VIDEO_GET_RECIEVE':
      return action.video || false;

    case 'VIDEO_CREATE_RECIEVE':
      return action.video || false;

    case 'VIDEO_UPDATE_REQUEST':
      return action.video;

    case 'VIDEO_POPULATE_BLANK':
      return action.video;

    case 'ASSET_REVERT_REQUEST':
      return Object.assign({}, state, {
        data: Object.assign({}, state.data, {
          activeVersion: action.assetVersion
        })
      })

    default:
      return state;
  }
}
