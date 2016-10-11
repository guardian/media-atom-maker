import React from 'react';
import { render } from 'react-dom';
import {Main} from './components/Main/Main';

import '../styles/index.scss';

const element = document.getElementById('react-mount');

render(<Main />, element);