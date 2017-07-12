import React from 'react';

export default class Help extends React.Component {
  helpPages = [
    {
      url: 'https://goo.gl/vzTc3s',
      text: 'How to use this Tool'
    },
    {
      url: 'https://goo.gl/forms/0KoeGOW64584Bydm2',
      text: 'Contact the Team'
    },
    {
      url: 'https://goo.gl/g2FUxn',
      text: 'Publishing without Pluto'
    },
    {
      url: 'https://docs.google.com/document/d/1Ey9BcoeUsZSTnVomQCRnGwdlO7akVceJZUYJihPd4xk/edit#',
      text: 'HOW TO FIX - Media Atom Maker creating invalid Composer Pages'
    },
    {
      url: 'https://docs.google.com/document/d/1jJuO09QO4GaYC0yirh8Mce2fOd2u0IdTXPF4OmtvD9M/edit#',
      text: 'Commercial FAQs'
    }
  ];

  renderLink({ url, text }) {
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

  renderLinkList() {
    return (
      <ul>
        {this.helpPages.map(page => (
          <li key={page.url}>{this.renderLink(page)}</li>
        ))}
      </ul>
    );
  }

  render() {
    return (
      <div className="container">
        <h1>Help</h1>
        {this.renderLinkList()}
      </div>
    );
  }
}
