import React from 'react';

export default class Help extends React.Component {
  getLink({ url, text }) {
    return (
      <a
        className="button__secondary"
        target="_blank"
        rel="noopener noreferrer"
        href={url}
      >
        {text}
      </a>
    );
  }

  render() {
    return (
      <div className="container">
        <h1>Help</h1>
        <ul>
          <li>
            {this.getLink({
              url: 'https://goo.gl/vzTc3s',
              text: 'How to use this Tool'
            })}
          </li>
          <li>
            {this.getLink({
              url: 'https://goo.gl/forms/0KoeGOW64584Bydm2',
              text: 'Contact the Team'
            })}
          </li>
          <li>
            {this.getLink({
              url: 'https://goo.gl/g2FUxn',
              text: 'Publishing without Pluto'
            })}
          </li>
        </ul>
      </div>
    );
  }
}
