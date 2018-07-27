import React from 'react';

export function GuardianLogo() {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="128"
      height="128"
      viewBox="0 0 128 128">
        <defs>
          <clipPath id="a">
            <path fill="none" d="M0 0h128v128H0z"/>
          </clipPath>
        </defs>
        <g clip-path="url(#a)">
          <path fill="#1d1d1b" d="M64 0a64 64 0 1 0 64 64A64.1 64.1 0 0 0 64 0m10.9 15.8c9 1.3 18.9 7 22.7 11v18.6h-2L74.9 17.9zM68.1 17h-.3C53.3 17 45 37.1 45.4 64.2c-.4 27.1 7.9 47.2 22.4 47.2h.3v2.1a48.3 48.3 0 0 1-51-49.3 48.3 48.3 0 0 1 51-49.3zm36.4 51.5l-6.9 2.9v30.7a52.7 52.7 0 0 1-22.7 11.1V70.8l-6.8-2.5v-1.9h36.4z"/>
        </g>
    </svg>
  );
}

export class FrontendIcon extends React.Component {
  render() {
    return (
      <span className="icon svg-icon">
        <GuardianLogo />
      </span>
    );
  }
}

export class ComposerIcon extends React.Component {
  render() {
    return (
      <span className="icon svg-icon">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="128"
          height="128"
          viewBox="0 0 128 128"
        >
          <path
            fill="#231F20"
            d="M12.2 64.8C12.2 16.6 45.2 0 76.8 0c15.3 0 28 2.4 35.6 5.4l1.2 33.4H98l-8-19.2c-3.5-1.5-8.3-3-14.2-3C58.4 16.6 49.6 29 49.6 62c0 38.7 8.4 49.7 27.2 49.7 6 0 11.5-1.8 14.8-3.5l8-20h15l-1.2 33c-8.8 4-20.5 6.8-40 6.8-34.2 0-61.2-17.8-61.2-63.2z"
          />
        </svg>
      </span>
    );
  }
}

export class ViewerIcon extends React.Component {
  render() {
    return (
      <span className="icon svg-icon">
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="32"
          height="32"
          viewBox="0 0 32 32"
        >
          <g fill="#FFF">
            <path d="M16 5.2C5.7 5.2 0 16 0 16s5.7 10.8 16 10.8S32 16 32 16 26.3 5.2 16 5.2zm0 18.4c-4.4 0-8-3.6-8-8s3.6-8 8-8 8 3.6 8 8-3.6 8-8 8z" />
            <path d="M16 11.9c-2.1 0-3.8 1.7-3.8 3.8 0 2.1 1.7 3.7 3.8 3.7 2.1 0 3.7-1.7 3.7-3.7.1-2.1-1.6-3.8-3.7-3.8z" />
          </g>
        </svg>
      </span>
    );
  }
}

export default class Icon extends React.Component {
  renderText() {
    if (this.props.children) {
      return (
        <span
          className={`icon--text responsive--${this.props.textClass || 'optional'}`}
        >
          {this.props.children}
        </span>
      );
    }
  }

  render() {
    if (!this.props.icon) {
      return;
    }

    const props = Object.assign({}, this.props);
    props.className = props.className
      ? `${props.className} responsive`
      : 'responsive';

    return (
      <span className={props.className}>
        <i className="icon responsive--primary" onClick={props.onClick}>{props.icon}</i>
        {this.renderText()}
      </span>
    );
  }
}
