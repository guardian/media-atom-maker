import React from 'react';
import {Route, IndexRoute} from 'react-router';

import Atoms from './components/Atoms/Atoms';
import AtomEdit from './components/AtomEdit/AtomEdit';

import ReactApp from './components/ReactApp';


export default [
  <Route path='/video' component={ReactApp}>
    <Route path='/video/atoms'       component={Atoms} />
    <Route path='/video/atoms/:id'   component={AtomEdit} />
    <IndexRoute                     component={Atoms} />
  </Route>
];
