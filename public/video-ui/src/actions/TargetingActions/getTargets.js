import TargetingApi from '../../services/TargetingApi';
import {receiveGetTarget, requestGetTargets} from "../../slices/targeting";
import {showError} from "../../slices/error";


export function getTargets(video) {
  return dispatch => {
    dispatch(requestGetTargets());
    return TargetingApi.getTargets(video)
      .then(res => dispatch(receiveGetTarget(res)))
      .catch(err => dispatch(showError('Failed to get Target', err)));
  }
}
