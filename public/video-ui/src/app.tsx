import * as Sentry from '@sentry/browser';
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
const { stage, sentryDsn, sentryEnabled } = getAppConfig();
const sentryEnvironment = stage.toLowerCase();

// publish uncaught errors to sentry.io. Whether Sentry is on is decided
// server-side (see util.SentryConfig) so the two can't disagree.
if (sentryEnabled) {
  Sentry.init({
    dsn: sentryDsn,
    environment: sentryEnvironment,
    integrations: [
      Sentry.browserTracingIntegration(),
      Sentry.browserProfilingIntegration(),
      Sentry.replayIntegration()
    ],
    // Sample down in PROD to control span volume; full sampling elsewhere.
    // Mirrors SentryConfig.tracesSampleRate on the server.
    tracesSampleRate: sentryEnvironment === 'prod' ? 0.1 : 1.0,
    // NB: `tracePropagationTargets` is deliberately unset. The SDK default is
    // already "same origin only", which is what SentryTracingFilter
    // needs, and the default correctly excludes protocol-relative URLs.
    // Profile automatically alongside sampled traces, so profiling volume
    // is bounded by tracesSampleRate above.
    profileSessionSampleRate: 1.0,
    profileLifecycle: 'trace',
    // No session replays; buffer in memory and only upload when an error occurs.
    replaysSessionSampleRate: 0,
    replaysOnErrorSampleRate: 1.0
    // Deliberately left on the v10 defaults, which attach user info and HTTP
    // bodies. This is an internal editorial tool behind pan-domain auth, and
    // that context is what makes production errors diagnosable. Revisit if
    // Sentry access ever widens beyond the team.
  });
}

setStore(store);

store.dispatch(updatePath(location.pathname));

render(
  <Provider store={store}>{routes}</Provider>,
  document.getElementById('react-mount')
);
