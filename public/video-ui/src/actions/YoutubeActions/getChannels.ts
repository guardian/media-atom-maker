import { Dispatch } from 'redux';
import { getYoutubeChannels, YouTubeChannelWithData } from '../../services/YoutubeApi';
import { KnownAction } from '../actions';

function requestChannels(): KnownAction {
  return {
    type: 'YT_CHANNELS_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveChannels(channels: YouTubeChannelWithData[]): KnownAction {
  return {
    type: 'YT_CHANNELS_GET_RECEIVE',
    channels: channels,
    receivedAt: Date.now()
  };
}

function errorReceivingCatetories(error: unknown): KnownAction {
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube channels',
    error: error,
    receivedAt: Date.now()
  };
}

export function getChannels() {
  return (dispatch: Dispatch<KnownAction>) => {
    dispatch(requestChannels());
    return getYoutubeChannels()
      .then(channels => dispatch(receiveChannels(channels)))
      .catch(error => dispatch(errorReceivingCatetories(error)));
  };
}
