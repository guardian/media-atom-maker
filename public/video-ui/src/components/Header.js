import React from 'react';
import {Link, IndexLink} from 'react-router';
import VideoSearch from './VideoSearch/VideoSearch';
import VideoPublishBar from './VideoPublishBar/VideoPublishBar';

export default class Header extends React.Component {

  publishVideo = () => {
    this.props.publishVideo(this.props.video.id);
  };

  renderHomeAndSearch() {
    return (
      <div className="flex-container">
        <Link to="/" className="topbar__home-link" title="Home">
          Home
        </Link>

        <VideoSearch {...this.props}/>
      </div>
    );
  }

  renderFeedback() {
    return (
      <nav className="topbar__nav-link">
        <a className="btn topbar__feedback"
           target="_blank"
           href="https://goo.gl/forms/0KoeGOW64584Bydm2">
          Give feedback
        </a>
      </nav>
    );
  }

  renderCreateVideo() {
    return (
      <nav className="topbar__nav-link">
        <Link className="btn" to="/videos/create">
          <i className="icon icon__add">add</i>
          Create new video
        </Link>
      </nav>
    );
  }

  render () {

    if (!this.props.showPublishedState) {
      return (
        <header className="topbar flex-container">

          {this.renderHomeAndSearch()}

          <div className="flex-spacer"></div>

          <div className="flex-container">
            {this.renderFeedback()}
            {this.renderCreateVideo()}
          </div>

        </header>
      );
    } else {
      return (
        <header className="topbar flex-container">

          {this.renderHomeAndSearch()}

          <VideoPublishBar className="flex-grow"
            video={this.props.video}
            saveState={this.props.saveState}
            publishVideo={this.publishVideo} />

          <div className="flex-container">
            {this.renderFeedback()}
            {this.renderCreateVideo()}
          </div>

        </header>
      );

    }
  }
}
