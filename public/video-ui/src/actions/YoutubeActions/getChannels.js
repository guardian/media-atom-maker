import { getYoutubeChannels } from '../../services/YoutubeApi';
import {channelsReceived} from "../../slices/youtubeSlice";

function errorReceivingCatetories(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube channels',
    error: error,
    receivedAt: Date.now()
  };
}

export function getChannels() {
  return dispatch => {
    return getYoutubeChannels()
      .then(channels => dispatch(channelsReceived(channels)))
      .catch(error => dispatch(errorReceivingCatetories(error)));

  };
}
