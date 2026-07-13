import React from 'react';

export default class VideoItemError extends React.Component {
  render() {
    return (
      <li className="grid__item">
        <div className="grid__image__placeholder error">
          <p>Unexpected error rendering this video</p>
        </div>
      </li>
    );
  }
}