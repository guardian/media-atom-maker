import TargetingApi from '../../services/TargetingApi';
import {receiveCreateTarget} from "../../slices/targeting";
import {showError} from "../../slices/error";

function requestCreateTarget() {
  return {
    type: 'TARGETING_POST_REQUEST',
    receivedAt: Date.now()
  };
}

export function createTarget(video) {
  return dispatch => {
    dispatch(requestCreateTarget());
    return TargetingApi.createTarget(video)
      .then(res => dispatch(receiveCreateTarget(res)))
      .catch(err => dispatch(showError(`Failed to create Target`, err)));
  }
}
