import React from 'react';
import {Link} from 'react-router';
import VideoSearch from './VideoSearch/VideoSearch';
import VideoPublishBar from './VideoPublishBar/VideoPublishBar';
import VideoUpload from './VideoUpload/VideoUpload';
import Icon from './Icon';

export default class Header extends React.Component {

  publishVideo = () => {
    this.props.publishVideo(this.props.video.id);
  };

  renderProgress() {
    if(this.props.s3Upload.total) {
      return <progress className="topbar__progress" max={this.props.s3Upload.total} value={this.props.s3Upload.progress} />;
    } else {
      return false;
    }
  }

  renderHome() {
    return (
      <div className="flex-container topbar__global">
        <Link to="/" className="topbar__home-link" title="Home"></Link>
      </div>
    );
  }

  renderSearch() {
    return (
      <div className="flex-container topbar__global">
        <VideoSearch {...this.props}/>
      </div>
    );
  }

  renderHeaderBack() {
    return (
      <div className="flex-container topbar__global">
        <Link to={`/videos/${this.props.video.id}`} className="button" title="Back"><Icon className="icon icon__back" icon="keyboard_arrow_left"></Icon></Link>
        <span className="header__video__title">{this.props.video.title}</span>
      </div>
    );
  }

  renderEmbedButton() {
    // you can always select a video in 'preview' mode (this is used by the Pluto embed)
    // otherwise you should only be able to select published videos (when embedded inside Composer for example)

    const embedButton = <button type="button" className="btn" onSelectVideo={this.selectVideo} publishedVideo={this.props.publishedVideo} onClick={this.props.onSelectVideo}>Select this Video</button>;

    switch(this.props.embeddedMode) {
      case "preview":
        return embedButton;

      case "live":
        if(isVideoPublished(this.props.publishedVideo)) {
          return embedButton;
        } else {
          return <div>This atom cannot be embedded because it has not been published</div>;
        }

      default:
        return false;
    }
  }

  renderVideoTitle() {
    return (
      <div className="flex-container">
        <span className="header__video__title">{this.props.video.title}</span>
      </div>
    );
  }

  renderFeedback() {
    return (
      <nav className="topbar__nav-link">
        <a className="button__secondary"
           target="_blank"
           rel="noopener noreferrer"
           href="https://goo.gl/forms/0KoeGOW64584Bydm2">
          <Icon icon="bug_report">Feedback</Icon>
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

  renderHowTo() {
    return (
      <nav className="topbar__nav-link">
        <a className="button__secondary"
           target="_blank"
           rel="noopener noreferrer"
           href="https://docs.google.com/a/guardian.co.uk/document/d/1pqRpgIAAlcUMafbA3T7ZHg54kEs2E9XeANzRoNrwTrE/edit?usp=sharing">
          <Icon icon="live_help">How To</Icon>
        </a>
      </nav>
    );
  }

  renderCreateVideo() {
    return (
      <nav className="topbar__nav-link">
        <Link className="btn" to="/videos/create">
          <Icon icon="add">Create new video</Icon>
        </Link>
      </nav>
    );
  }

  render () {
    if (this.props.currentPath.endsWith("/upload")){
      return (
        <header className="topbar flex-container">
          {this.renderProgress()}
          {this.renderHeaderBack()}
        </header>
      );
    } if (!this.props.showPublishedState) {
      return (
        <header className="topbar flex-container">
          {this.renderProgress()}

          {this.renderHome()}
          {this.renderSearch()}

          <div className="flex-spacer"></div>

        <div className="flex-container">
            {this.renderCreateVideo()}
          </div>

          <div className="flex-container">
            {this.renderFeedback()}
            {this.renderHowTo()}
          </div>

        </header>
      );
    } if (this.props.embeddedMode === 'live'){
      return (
        <header className="topbar flex-container">
          <div>This is embed mode</div>
        </header>
      );
    } else {
      return (
        <header className="topbar flex-container">
          {this.renderProgress()}
          {this.renderHome()}

          <div>
            {this.renderVideoTitle()}
          </div>

          <VideoPublishBar className="flex-grow"
            video={this.props.video}
            publishedVideo={this.props.publishedVideo}
            saveState={this.props.saveState}
            publishVideo={this.publishVideo} />

          <div className="flex-container">
            {this.renderAuditLink()}
          </div>

        </header>
      );

    }
  }
}
