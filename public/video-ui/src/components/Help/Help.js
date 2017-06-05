import React from 'react';

export default class Help extends React.Component {
  renderHowTo() {
    return (
      <a
        className="button__secondary"
        target="_blank"
        rel="noopener noreferrer"
        href="https://goo.gl/vzTc3s"
      >
        How to use this Tool
      </a>
    );
  }

  renderFeedback() {
    return (
      <a
        className="button__secondary"
        target="_blank"
        rel="noopener noreferrer"
        href="https://goo.gl/forms/0KoeGOW64584Bydm2"
      >
        Contact the Team
      </a>
    );
  }

  render() {
    return (
      <div className="container">
        <h1>Help</h1>
        <ul>
          <li>
            {this.renderHowTo()}
          </li>
          <li>
            {this.renderFeedback()}
          </li>
        </ul>
      </div>
    );
  }
}
