import React from 'react';

import Header from './Header';

export default class ReactApp extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
        <div className="wrap">
          <Header/>
          <div>
            {this.props.children}
          </div>
        </div>
    );
  }
}
