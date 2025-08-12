import { getYoutubeCategories, YouTubeVideoCategory } from '../../services/YoutubeApi';
import Logger from '../../logger';
import { KnownAction } from '../actions';
import { Dispatch } from 'redux';

function requestCategories(): KnownAction {
  return {
    type: 'YT_CATEGORIES_GET_REQUEST',
    receivedAt: Date.now()
  };
}

function receiveCategories(categories: YouTubeVideoCategory[]): KnownAction {
  return {
    type: 'YT_CATEGORIES_GET_RECEIVE',
    categories: categories,
    receivedAt: Date.now()
  };
}

function errorReceivingCategories(error: unknown): KnownAction {
  Logger.error(error);
  return {
    type: 'SHOW_ERROR',
    message: 'Could not get YouTube categories',
    error: error,
    receivedAt: Date.now()
  };
}

export function getCategories() {
  return (dispatch: Dispatch<KnownAction>) => {
    dispatch(requestCategories());
    return getYoutubeCategories()
      .then(categories => {
        dispatch(receiveCategories(categories));
      })
      .catch(error => {
        dispatch(errorReceivingCategories(error));
      });
  };
}
