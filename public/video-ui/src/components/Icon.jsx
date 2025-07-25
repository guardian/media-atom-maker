import React from 'react';

export function GuardianLogo() {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="128"
      height="128"
      viewBox="0 0 128 128"
    >
      <defs>
        <clipPath id="a">
          <path fill="none" d="M0 0h128v128H0z" />
        </clipPath>
      </defs>
      <g clipPath="url(#a)">
        <path
          fill="#1d1d1b"
          d="M64 0a64 64 0 1 0 64 64A64.1 64.1 0 0 0 64 0m10.9 15.8c9 1.3 18.9 7 22.7 11v18.6h-2L74.9 17.9zM68.1 17h-.3C53.3 17 45 37.1 45.4 64.2c-.4 27.1 7.9 47.2 22.4 47.2h.3v2.1a48.3 48.3 0 0 1-51-49.3 48.3 48.3 0 0 1 51-49.3zm36.4 51.5l-6.9 2.9v30.7a52.7 52.7 0 0 1-22.7 11.1V70.8l-6.8-2.5v-1.9h36.4z"
        />
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

export class SubtitlesIcon extends React.Component {
  render() {
    return (
      <span>
        <svg
          width="68"
          height="52"
          viewBox="0 0 68 52"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M0 7.42857C0 3.33125 3.38819 0 7.55556 0H60.4444C64.6118 0 68 3.33125 68 7.42857V44.5714C68 48.6687 64.6118 52 60.4444 52H7.55556C3.38819 52 0 48.6687 0 44.5714V7.42857ZM23.6111 20.4286C25.2875 20.4286 26.7986 21.1366 27.8375 22.2857C28.8764 23.4348 30.6708 23.5277 31.8396 22.5062C33.0083 21.4848 33.1028 19.7205 32.0639 18.5714C29.9979 16.2964 26.9757 14.8571 23.6229 14.8571C17.366 14.8571 12.2896 19.8482 12.2896 26C12.2896 32.1518 17.366 37.1429 23.6229 37.1429C26.9757 37.1429 29.9979 35.7036 32.0639 33.4286C33.1028 32.2795 33.0083 30.5268 31.8396 29.4937C30.6708 28.4607 28.8882 28.5652 27.8375 29.7143C26.7986 30.8634 25.2875 31.5714 23.6111 31.5714C20.4826 31.5714 17.9444 29.0759 17.9444 26C17.9444 22.9241 20.4826 20.4286 23.6111 20.4286ZM40.6111 26C40.6111 22.9241 43.1493 20.4286 46.2778 20.4286C47.9542 20.4286 49.4653 21.1366 50.5042 22.2857C51.5431 23.4348 53.3375 23.5277 54.5062 22.5062C55.675 21.4848 55.7694 19.7205 54.7306 18.5714C52.6646 16.2964 49.6424 14.8571 46.2896 14.8571C40.0326 14.8571 34.9562 19.8482 34.9562 26C34.9562 32.1518 40.0326 37.1429 46.2896 37.1429C49.6424 37.1429 52.6646 35.7036 54.7306 33.4286C55.7694 32.2795 55.675 30.5268 54.5062 29.4937C53.3375 28.4607 51.5549 28.5652 50.5042 29.7143C49.4653 30.8634 47.9542 31.5714 46.2778 31.5714C43.1493 31.5714 40.6111 29.0759 40.6111 26Z"
            fill="#848484"
          />
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
          className={`icon--text responsive--${
            this.props.textClass || 'optional'
          }`}
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
      <span
        className={`${props.className} ${
          this.props.disabled ? 'disabled' : ''
        }`}
      >
        <i className="icon responsive--primary" onClick={props.onClick}>
          {props.icon}
        </i>
        {this.renderText()}
      </span>
    );
  }
}
