import React from 'react';
import { Link } from 'react-router';

type HelpPage = {
  url: string;
  text: string;
};

const helpPages: HelpPage[] = [
  {
    url: 'https://goo.gl/3tCyKB',
    text: 'How to use this Tool'
  },
  {
    url: 'https://docs.google.com/a/guardian.co.uk/document/d/1pZ1fh5aX9HRkDv9bNWQvRJZgaNuhmddRqpIkMrmM0FA/edit?usp=sharing',
    text: 'How to schedule Atoms'
  },
  {
    url: 'https://goo.gl/forms/0KoeGOW64584Bydm2',
    text: 'Contact the Team'
  },
  {
    url: 'https://docs.google.com/document/d/1jJuO09QO4GaYC0yirh8Mce2fOd2u0IdTXPF4OmtvD9M/edit#',
    text: 'Commercial FAQs'
  },
  {
    url: 'https://docs.google.com/document/d/1UZewPvXCuYwWkTlUy-fSN5r1Tdcg2XfBsXP4poMNfhw/edit',
    text: 'Useful YouTube Contacts'
  }
];

function renderLink({ url, text }: HelpPage) {
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

export default function Help() {
  return (
    <div className="container">
      <h1>Help</h1>
      <ul>
        <li>
          <Link className="button__secondary" to={'/training'}>
            Training
          </Link>
        </li>
        {helpPages.map(page => (
          <li key={page.url}>{renderLink(page)}</li>
        ))}
      </ul>
    </div>
  );
}
