import React from 'react';
import {Link} from 'react-router';
import VideoSearch from './VideoSearch/VideoSearch';
import VideoPublishBar from './VideoPublishBar/VideoPublishBar';

export default class Header extends React.Component {

  publishVideo = () => {
    this.props.publishVideo(this.props.video.id);
  };

  renderHomeAndSearch() {
    return (
      <div className="flex-container topbar__global">
        <Link to="/" className="topbar__home-link" title="Home"></Link>
        <VideoSearch {...this.props}/>
      </div>
    );
  }

  renderFeedback() {
    return (
      <nav className="topbar__nav-link">
        <a className="button__secondary"
           target="_blank"
           href="https://goo.gl/forms/0KoeGOW64584Bydm2">
          <i className="icon">bug_report</i> Feedback
        </a>
      </nav>
    );
  }

  renderAuditLink() {
    const auditLink = "/videos/" + this.props.video.id + "/audit";
    return (
      <nav className="topbar__nav-link topbar__functional">
        <Link activeClassName="topbar__nav-link--active" className="button__secondary" to={auditLink}>View audit trail</Link>
      </nav>
    );
  }

  renderCreateVideo() {
    return (
      <nav className="topbar__nav-link">
        <Link className="button__secondary" to="/videos/create">
          <i className="icon">add</i> Create new video
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
            publishedVideo={this.props.publishedVideo}
            saveState={this.props.saveState}
            publishVideo={this.publishVideo} />

          <div className="flex-container">
            {this.renderAuditLink()}
            {this.renderFeedback()}
            {this.renderCreateVideo()}
          </div>

        </header>
      );

    }
  }
}
