import {saveStateVals} from '../constants/saveStateVals'

export default function saveState(state = '', action) {
}
switch (action.type) {
  case 'SHOW_ERROR':
    return saveStateVals.error;

  case 'VIDEO_CREATE_REQUEST':
    return saveStateVals.inprogress;

  case 'VIDEO_CREATE_RECEIVE':
    return '';

  default:
    return state;
}
