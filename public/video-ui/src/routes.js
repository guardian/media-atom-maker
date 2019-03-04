import React from 'react';
import { Router, Route, browserHistory, IndexRedirect, Redirect } from 'react-router';

import Videos from './components/Videos/Videos';
import VideoDisplay from './components/Video/Display';
import VideoUpload from './components/VideoUpload/VideoUpload';
import Help from './pages/Help';
import Training from './pages/Training';
import ReactApp from './components/ReactApp';

export const routes = (
  <Router history={browserHistory}>
    <Route path="/" component={ReactApp}>
      <IndexRedirect to="/videos" />
      <Route path="/videos" component={Videos} />
      <Redirect from="/videos/create" to="/create" />
      <Route path="/videos/:id" component={VideoDisplay} />
      <Route path="/videos/:id/upload" component={VideoUpload} />
      <Route path="/create" component={VideoDisplay} mode="create" />
      <Route path="/help" component={Help} />
      <Route path="/training" component={Training} />
    </Route>
  </Router>
);
