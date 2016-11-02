import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';

import configureStore from './util/configureStore';
import {setStore} from './util/storeAccessor';
import {routes} from './routes';

import '../styles/main.scss';

function extractConfigFromPage() {

  const configEl = document.getElementById('config');

  if (!configEl) {
    return {};
  }

  return JSON.parse(configEl.innerHTML);
}

const store = configureStore();
const config = extractConfigFromPage();

setStore(store);

store.dispatch({
  type:       'CONFIG_RECEIVED',
  config:     config,
  receivedAt: Date.now()
});


render(
    <Provider store={store}>
      {routes}
    </Provider>
    , document.getElementById('react-mount'));