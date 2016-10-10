import React from 'react';
import {Link, IndexLink} from 'react-router';

export default class Header extends React.Component {
  constructor(props) {
    super(props);
  }

  render () {
    return (
        <header className="top-toolbar">
          <Link to="/" className="home-logo">
            <span className="home-logo__text-large">Home</span>
            <span className="home-logo__text-small">Sweet home</span>
          </Link>

          <div className="header__children">
            <nav className="links">
              <IndexLink activeClassName="links__item--active" className="links__item top-toolbar__item--highlight" to="/">Campaigns</IndexLink>
              <HeaderMenuItem to="/embeds">Suggested Embeds</HeaderMenuItem>
            </nav>
          </div>
        </header>
    );
  }
}

class HeaderMenuItem extends React.Component {
  constructor(props) {
    super(props)
  }

  render() {
    return (
        <Link
            to={this.props.to}
            activeClassName="links__item--active"
            className="links__item top-toolbar__item--highlight"
        >{this.props.children}</Link>
    )
  }
}
