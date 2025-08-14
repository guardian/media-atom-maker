import { browserHistory } from 'react-router';
import { routerMiddleware } from 'react-router-redux';
import { storeMiddleware } from './storeMiddleware';

import reducers from '../reducers/reducers';
import { configureStore } from '@reduxjs/toolkit';
import {youtubeApi} from "../slices/youtubeSlice";

export function setupStore() {
  return configureStore({
    reducer: reducers,
    middleware: getDefaultMiddleware => {
      return getDefaultMiddleware().concat(
        youtubeApi.middleware,
        routerMiddleware(browserHistory),
        storeMiddleware
      );
    }
  });
}
