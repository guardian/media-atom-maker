import Raven from 'raven-js';
import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import { browserHistory } from 'react-router';
import { syncHistoryWithStore } from 'react-router-redux';
import { routes } from './routes';
import { updatePath } from './slices/path';
import { getAppConfig } from './util/config';
import { setupStore } from './util/setupStore';
import { setStore } from './util/storeAccessor';

import '../styles/main.scss';

const store = setupStore();
syncHistoryWithStore(browserHistory, store);
const { stage, ravenUrl } = getAppConfig();

// publish uncaught errors to sentry.io
if (stage !== 'DEV') Raven.config(ravenUrl).install();

setStore(store);

store.dispatch(updatePath(location.pathname));

render(
  <Provider store={store}>{routes}</Provider>,
  document.getElementById('react-mount')
);
