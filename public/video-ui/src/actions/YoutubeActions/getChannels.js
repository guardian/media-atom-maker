import {getYoutubeChannels} from '../../services/YoutubeApi';

function requestChannels() {
  return {
    type: 'YT_CHANNELS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveChannels(channels) {
  return {
    type: 'YT_CHANNELS_GET_RECEIVE',
    channels: channels,
    receivedAt: Date.now()
  };
}

function errorReceivingCatetories(error) {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube channels',
    error: error,
    receivedAt: Date.now()
  }
}

export function getChannels() {
  return dispatch => {
    dispatch(requestChannels());
    return getYoutubeChannels()
      .catch(error => dispatch(errorReceivingCatetories(error)))
      .then(channels => dispatch(receiveChannels(channels)));
  };
}
