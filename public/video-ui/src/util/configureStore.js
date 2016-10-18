import { compose, createStore, applyMiddleware } from 'redux';
import thunkMiddleware from 'redux-thunk';

import rootReducer from '../reducers/rootReducer';

const createStoreWithMiddleware = compose(
    applyMiddleware(
        thunkMiddleware
    ),
    window.devToolsExtension ? window.devToolsExtension() : f => f
)(createStore);

export default function configureStore(initialState) {
  return createStoreWithMiddleware(rootReducer, initialState);
}
