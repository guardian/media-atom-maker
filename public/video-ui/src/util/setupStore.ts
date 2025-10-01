import { browserHistory } from 'react-router';
import { routerMiddleware } from 'react-router-redux';
import { storeMiddleware } from './storeMiddleware';

import reducers from '../slices/reducers';
import { configureStore } from '@reduxjs/toolkit';

export function setupStore() {
  return configureStore({
    reducer: reducers,
    middleware: getDefaultMiddleware => {
      return getDefaultMiddleware().concat(
        routerMiddleware(browserHistory),
        storeMiddleware
      );
    }
  });
}

export type RootState = ReturnType<ReturnType<typeof setupStore>['getState']>;
export type AppDispatch = ReturnType<typeof setupStore>['dispatch'];
