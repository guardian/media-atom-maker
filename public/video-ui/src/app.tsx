import * as Sentry from '@sentry/browser';
import React from 'react';
import { render } from 'react-dom';
import { Provider } from 'react-redux';
import { browserHistory } from 'react-router';
import { syncHistoryWithStore } from 'react-router-redux';
import { routes } from './routes';
import { updatePath } from './slices/path';
import { NETWORK_FAILURE_MESSAGE } from './slices/error';
import { getAppConfig } from './util/config';
import { setupStore } from './util/setupStore';
import { setStore } from './util/storeAccessor';

import '../styles/main.scss';

const store = setupStore();
syncHistoryWithStore(browserHistory, store);
const {
  stage,
  sentryDsn,
  sentryEnabled,
  sentryTracesSampleRate,
  sentryReplayEnabled,
  userEmail
} = getAppConfig();
const sentryEnvironment = stage;

// `pagehide` rather than `beforeunload` so it also fires on bfcache navigation.
let pageIsUnloading = false;
window.addEventListener('pagehide', () => {
  pageIsUnloading = true;
});
window.addEventListener('pageshow', () => {
  pageIsUnloading = false;
});

/** In-flight requests abort when the user navigates away, and every request
 * fails while the browser is offline. Both surface as fetch failures that no
 * one can act on, and they drown out genuine API outages. */
const isUnactionableNetworkFailure = (event: Sentry.ErrorEvent): boolean =>
  (pageIsUnloading || navigator.onLine === false) &&
  (event.exception?.values ?? []).some(
    value =>
      value.type === 'TypeError' &&
      NETWORK_FAILURE_MESSAGE.test(value.value ?? '')
  );

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
    // Server-owned value keeps browser and server trace sampling aligned.
    tracesSampleRate: sentryTracesSampleRate,
    // NB: `tracePropagationTargets` is deliberately unset. The SDK default is
    // already "same origin only", which is what SentryTracingFilter
    // needs, and the default correctly excludes protocol-relative URLs.
    // Profile automatically alongside sampled traces, so profiling volume
    // is bounded by tracesSampleRate above.
    profileSessionSampleRate: 1.0,
    profileLifecycle: 'trace',
    // No session replays; buffer in memory and only upload when an error occurs.
    replaysSessionSampleRate: 0,
    replaysOnErrorSampleRate: sentryReplayEnabled ? 1.0 : 0,
    beforeSend: event => (isUnactionableNetworkFailure(event) ? null : event)
  });

  // Staff-only tool, so the pan-domain email is the useful identifier when
  // triaging. No other user fields are sent.
  Sentry.setUser({ email: userEmail });
}

setStore(store);

store.dispatch(updatePath(location.pathname));

render(
  <Provider store={store}>{routes}</Provider>,
  document.getElementById('react-mount')
);
