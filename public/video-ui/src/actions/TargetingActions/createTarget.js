import TargetingApi from '../../services/TargetingApi';
import {receiveCreateTarget, requestCreateTarget} from "../../slices/targeting";
import {showError} from "../../slices/error";

export function createTarget(video) {
  return dispatch => {
    dispatch( requestCreateTarget());
    return TargetingApi.createTarget(video)
      .then(res => dispatch(receiveCreateTarget(res)))
      .catch(err => dispatch(showError(`Failed to create Target`, err)));
  }
}
