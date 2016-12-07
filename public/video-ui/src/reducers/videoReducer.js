export default function video(state = null, action) {
  switch (action.type) {

    case 'VIDEO_GET_RECEIVE':
      return action.video || false;

    case 'VIDEO_CREATE_RECEIVE':
      return action.video || false;

    case 'VIDEO_UPDATE_REQUEST':
      return action.video;

    case 'VIDEO_SAVE_REQUEST':
      return action.video;

    case 'VIDEO_POPULATE_BLANK':
      return action.video;

    case 'VIDEO_PUBLISH_RECEIVE':
      return Object.assign({}, state, {
        contentChangeDetails: action.video.contentChangeDetails
      });

    case 'VIDEO_SAVE_RECEIVE':
      return Object.assign({}, state, {
        contentChangeDetails: action.video.contentChangeDetails
      });

    case 'ASSET_REVERT_REQUEST':
      return Object.assign({}, state, {
        activeVersion: action.assetVersion
      });

    case 'VIDEO_PAGE_POST_RECEIVE':
      return Object.assign({}, action.video);

    default:
      return state;
  }
}
