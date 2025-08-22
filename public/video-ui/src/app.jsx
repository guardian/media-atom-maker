import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import qs from 'querystringify';
import Raven from 'raven-js';
import { browserHistory } from 'react-router';
import { syncHistoryWithStore } from 'react-router-redux';
import { setConfig } from './slices/config';
import { setupStore } from './util/setupStore';
import { setStore } from './util/storeAccessor';
import { extractConfigFromPage } from './util/config';
import { routes } from './routes';
import { updatePath } from './slices/path';

import '../styles/main.scss';

const store = setupStore();
syncHistoryWithStore(browserHistory, store);
const config = extractConfigFromPage();

// publish uncaught errors to sentry.io
if (config.stage === 'PROD') Raven.config(config.ravenUrl).install();

setStore(store);

store.dispatch(
  setConfig(Object.assign({}, extractConfigFromPage(), {
    embeddedMode: qs.parse(location.search).embeddedMode
  }))
);


store.dispatch({
  type: 'PATH_UPDATE',
  path: location.pathname,
  receivedAt: Date.now()
});

store.dispatch(
  updatePath(location.pathname)
);

render(
  <Provider store={store}>
    {routes}
  </Provider>,
  document.getElementById('react-mount')
);
