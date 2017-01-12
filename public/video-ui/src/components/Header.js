import React from 'react';
import {Link, IndexLink} from 'react-router';
import VideoSearch from './VideoSearch/VideoSearch';

export default class Header extends React.Component {

  render () {
    return (
        <header className="topbar">

          <div className="topbar__container">
            <Link to="/video/videos" className="topbar__home-link" title="Home">
              Home
            </Link>

            <VideoSearch {...this.props}/>
          </div>

          <div className="topbar__container">
            <nav className="topbar__nav topbar__feedback">
              <a className="topbar__nav-link"
                 target="_blank"
                 href="https://goo.gl/forms/0KoeGOW64584Bydm2">
                Give us feedback?
              </a>
            </nav>

            <nav className="topbar__nav">
              <i className="icon icon__add">add</i>
              <Link activeClassName="topbar__nav-link--active" className="topbar__nav-link" to="/video/videos/create">Create new video</Link>
            </nav>
          </div>
        </header>
    );
  }
}
