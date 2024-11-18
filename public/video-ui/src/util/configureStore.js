import { compose, createStore, applyMiddleware } from 'redux';
import thunkMiddleware from 'redux-thunk';
import { browserHistory } from 'react-router';
import { routerMiddleware } from 'react-router-redux';
import { storeMiddleware } from './storeMiddleware';

import rootReducer from '../reducers/rootReducer';

export function configureStore() {
  const router = routerMiddleware(browserHistory);
  const store = createStore(
    rootReducer,
    compose(
      applyMiddleware(thunkMiddleware),
      applyMiddleware(router),
      applyMiddleware(storeMiddleware),
      window.devToolsExtension ? window.devToolsExtension() : f => f
    )
  );

  return store;
}
