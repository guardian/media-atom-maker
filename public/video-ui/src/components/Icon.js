import React from 'react';

export function GuardianLogo() {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="128"
      height="128"
      viewBox="0 0 128 128"
    >
      <path
        fill="#231F20"
        d="M25.7 30.4c0 11.6 6.2 19.4 14.6 25l.4.3c-5 3.4-15 11.4-15 20.5 0 7 4.4 13.6 14 15.7-10.2 1.8-21.2 6.3-21.2 17.5v1C7.3 98.7 0 83.4 0 65.7c0-26 15.6-48 37.2-58.4-7 5.3-11.5 13-11.5 23zm50.5 66.8c8 0 12.4 4 12.4 10 0 7-5 12.2-24 12.2-16.6 0-22.3-5.6-22.3-12 0-4.6 2-10.2 10.2-10.2h23.7zm32.6 14c2.2-4.4 3.5-9.5 3.5-15.2 0-17.4-9.3-25-29-25H51.8c-3.4 0-6.3-2.8-6.3-5.8 0-2.2 1.8-4.7 4-6.4 4.8 1.6 8.8 1.7 14.5 1.7 22.5 0 38-11 37.8-30.2 0-11-4.8-18.7-12.5-23.3C112 16.7 128 39 128 65.8c0 18-7.4 34-19.2 45.5zM64 50c-6.3 0-11.5-1.5-11.5-19.4 0-18 5.2-20.2 11.5-20.2 6.5 0 11.5 2.8 11.5 20.2 0 17.5-5.2 19.4-11.5 19.4z"
      />
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
        <i className="icon responsive--primary">{props.icon}</i>
        {this.renderText()}
      </span>
    );
  }
}
