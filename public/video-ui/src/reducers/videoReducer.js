import { blankVideoData } from '../constants/blankVideoData';

export default function video(state = null, action) {
  switch (action.type) {
    case 'VIDEO_GET_RECEIVE':
      return action.video
        ? Object.assign({}, blankVideoData, {source: null}, action.video)
        : false;

    case 'VIDEO_CREATE_RECEIVE':
      return action.video
        ? Object.assign({}, blankVideoData, action.video)
        : false;

    case 'VIDEO_UPDATE_REQUEST':
      return Object.assign({}, blankVideoData, action.video);

    case 'VIDEO_SAVE_REQUEST':
      return Object.assign({}, blankVideoData, action.video);

    case 'VIDEO_POPULATE_BLANK':
      return Object.assign({}, blankVideoData, {
        type: 'media'
      });

    case 'ASSET_REVERT_REQUEST':
      return Object.assign({}, state, {
        activeVersion: action.assetVersion
      });

    case 'ASSET_CREATE_RECEIVE':
      return Object.assign({}, state, {
        assets: action.video.assets
      });

    default:
      return state;
  }
}
