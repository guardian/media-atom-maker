import React from 'react';
import {Link, IndexLink} from 'react-router';

export default class Header extends React.Component {
  constructor(props) {
    super(props);
  }

  render () {
    return (
        <header className="top-toolbar">
          <Link to="/video/atoms" className="home-logo">
            <span className="home-logo__text-large">Back to</span>
            <span className="home-logo__text-small">Atoms</span>
          </Link>

          <div className="header__children">
            <nav className="links">
              <IndexLink activeClassName="links__item--active" className="links__item top-toolbar__item--highlight" to="/video">Media Atoms</IndexLink>
            </nav>
          </div>
        </header>
    );
  }
}
