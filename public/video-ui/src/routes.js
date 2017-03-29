import React from 'react';
import {Router, Route, browserHistory, IndexRedirect} from 'react-router';

import Videos from './components/Videos/Videos';
import VideoDisplay from './components/Video/Display';
import VideoCreate from './components/Video/Create';
import VideoAuditTrail from './components/VideoAuditTrail/VideoAuditTrail';
import VideoUpload from './components/VideoUpload/VideoUpload';
import VideoPlutoList from './components/VideoPluto/VideoPlutoList';
import ReactApp from './components/ReactApp';


export const routes = (
  <Router history={browserHistory}>
    <Route path="/" component={ReactApp}>
      <IndexRedirect to="/videos" />
      <Route path="/videos" component={Videos} />
      <Route path="/videos/create" component={VideoCreate} />
      <Route path="/videos/pluto-list" component={VideoPlutoList} />
      <Route path="/videos/:id" component={VideoDisplay} />
      <Route path="/videos/:id/audit" component={VideoAuditTrail} />
      <Route path="/videos/:id/upload" component={VideoUpload} />
    </Route>
  </Router>
);
