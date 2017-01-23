import React from 'react';
import {Link, IndexLink} from 'react-router';
import VideoSearch from './VideoSearch/VideoSearch';

export default class Header extends React.Component {

  renderHomeAndSearch() {
    return (
      <div className="topbar__container">
        <Link to="/" className="topbar__home-link" title="Home">
          Home
        </Link>

        <VideoSearch {...this.props}/>
      </div>
    );
  }

  renderFeedback() {
    return (
      <nav className="topbar__nav topbar__feedback">
        <a className="topbar__nav-link"
           target="_blank"
           href="https://goo.gl/forms/0KoeGOW64584Bydm2">
          Give feedback
        </a>
      </nav>
    );
  }

  renderCreateVideo() {
    return (
      <nav className="topbar__nav">
        <i className="icon icon__add">add</i>
        <Link activeClassName="topbar__nav-link--active" className="topbar__nav-link" to="/videos/create">Create new video</Link>
      </nav>

    );

  }

  render () {

    if (!this.props.showPublishedState) {
      return (
        <header className="topbar">

          {this.renderHomeAndSearch()}

          <div className="topbar__container">
            {this.renderFeedback()}
            {this.renderCreateVideo()}
          </div>

        </header>
      );
    } else {
      return (
        <header className="topbar">

          {this.renderHomeAndSearch()}

          <div className="topbar__container">
            {this.renderFeedback()}
          </div>

        </header>
      );

    }
  }
}
