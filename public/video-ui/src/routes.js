import React from 'react';
import { Router, Route, browserHistory, IndexRedirect, Redirect } from 'react-router';

import Search from './pages/Search';
import Video from './pages/Video';
import Upload from './pages/Upload/';
import Help from './pages/Help';
import Training from './pages/Training';
import ReactApp from './components/ReactApp';

export const routes = (
  <Router history={browserHistory}>
    <Route path="/" component={ReactApp}>
      <IndexRedirect to="/videos" />
      <Route path="/videos" component={Search} />
      <Redirect from="/videos/create" to="/create" />
      <Route path="/videos/:id" component={Video} />
      <Route path="/videos/:id/upload" component={Upload} />
      <Route path="/create" component={Video} mode="create" />
      <Route path="/help" component={Help} />
      <Route path="/training" component={Training} />
    </Route>
  </Router>
);
