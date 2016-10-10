import React from 'react';
import {Route, IndexRoute} from 'react-router';

import Atoms from './components/Atoms/Atoms';

import ReactApp from './components/ReactApp';


export default [
  <Route path='/beta' component={ReactApp}>
    <Route path='/beta/atoms'   component={Atoms} />
    <IndexRoute                 component={Atoms} />
  </Route>
];
