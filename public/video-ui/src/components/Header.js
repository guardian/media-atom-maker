import React from 'react';
import {Link, IndexLink} from 'react-router';

export default class Header extends React.Component {
  constructor(props) {
    super(props);
  }

  render () {
    return (
        <header className="topbar">

          <div className="topbar__container">
            <Link to="/video/videos" className="topbar__home-link" title="Home">
              Home
            </Link>
          </div>

          <div className="topbar__container">

            <nav className="topbar__nav">
              <Link activeClassName="topbar__nav-link--active" className="topbar__nav-link" to="/video/videos/create">Create new video</Link>
            </nav>

          </div>
        </header>
    );
  }
}
