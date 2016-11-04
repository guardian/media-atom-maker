import {saveStateVals} from '../constants/saveStateVals'

export default function saveState(state = '', action) {

  switch (action.type) {
    case 'SHOW_ERROR':
      return saveStateVals.error;

    case 'VIDEO_CREATE_REQUEST':
      return saveStateVals.inprogress;

    case 'VIDEO_PUBLISH_REQUEST':
      return saveStateVals.inprogress;

    case 'ASSET_REVERT_REQUEST':
      return saveStateVals.inprogress;

    case 'VIDEO_SAVE_REQUEST':
      return saveStateVals.inprogress;

    case 'VIDEO_PUBLISH_RECEIVE':
      return '';

    case 'VIDEO_CREATE_RECEIVE':
      return '';

    case 'ASSET_REVERT_RECEIVE':
      return '';

    case 'VIDEO_SAVE_RECEIVE':
      return '';

    default:
      return state;
  }
}
