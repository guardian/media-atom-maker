import { getYoutubeCategories } from '../../services/YoutubeApi';
import Logger from '../../logger';
import {categoriesReceived} from "../../slices/youtubeSlice";

function errorReceivingCategories(error) {
  Logger.error(error);
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube categories',
    error: error,
    receivedAt: Date.now()
  };
}

export function getCategories() {
  return dispatch => {
    return getYoutubeCategories()
      .then(res => {
        dispatch(categoriesReceived(res));
      })
      .catch(error => {
        dispatch(errorReceivingCategories(error));
      });
  };
}
