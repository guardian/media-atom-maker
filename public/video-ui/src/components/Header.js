import React from 'react';
import { Link } from 'react-router';
import VideoSearch from './VideoSearch/VideoSearch';
import VideoPublishBar from './VideoPublishBar/VideoPublishBar';
import AdvancedActions from './Videos/AdvancedActions';
import ComposerPageCreate from './Videos/ComposerPageCreate';
import Icon from './Icon';
import { Presence } from '../util/Presence';

export default class Header extends React.Component {
  state = { presence: null };

  componentDidMount() {
    if (this.props.video.id) {
      this.setState({
        presence: new Presence(this.props.video.id, this.props.presenceConfig)
      });
    }
  }

  componentDidUpdate(prevProps) {
    const current = this.props.video.id;
    const previous = prevProps.video.id;

    if (current !== previous) {
      if (this.state.presence) {
        this.state.presence.close();
        this.setState({ presence: null });
      }

      if (current) {
        this.setState({
          presence: new Presence(this.props.video.id, this.props.presenceConfig)
        });
      }
    }
  }

  publishVideo = () => {
    this.props.publishVideo(this.props.video.id);
  };

  renderProgress() {
    if (this.props.s3Upload.total) {
      // Start prompting the user about reloading the page
      window.onbeforeunload = () => {
        return false;
      };

      return (
        <progress
          className="topbar__progress"
          max={this.props.s3Upload.total}
          value={this.props.s3Upload.progress}
        />
      );
    } else {
      // Stop prompting the user. The upload continues server-side
      window.onbeforeunload = undefined;
      return false;
    }
  }

  renderHome() {
    return (
      <div className="flex-container topbar__global">
        <Link to="/" className="topbar__home-link" title="Home" />
      </div>
    );
  }

  renderSearch() {
    return (
      <div className="flex-container topbar__global">
        <VideoSearch {...this.props} />
      </div>
    );
  }

  renderHeaderBack() {
    return (
      <div className="flex-container topbar__global">
        <Link
          to={`/videos/${this.props.video.id}`}
          className="button"
          title="Back"
        >
          <Icon className="icon icon__back" icon="keyboard_arrow_left" />
        </Link>
        <span className="header__video__title">{this.props.video.title}</span>
      </div>
    );
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
        <a
          className="button__secondary"
          target="_blank"
          rel="noopener noreferrer"
          href="https://goo.gl/forms/0KoeGOW64584Bydm2"
        >
          <Icon icon="bug_report">Feedback</Icon>
        </a>
      </nav>
    );
  }

  renderAuditLink() {
    const auditLink = '/videos/' + this.props.video.id + '/audit';
    return (
      <nav className="topbar__nav-link topbar__functional">
        <Link
          activeClassName="topbar__nav-link--active"
          className="button__secondary"
          to={auditLink}
        >
          View audit trail
        </Link>
      </nav>
    );
  }

  renderHowTo() {
    return (
      <nav className="topbar__nav-link">
        <a
          className="button__secondary"
          target="_blank"
          rel="noopener noreferrer"
          href="https://docs.google.com/a/guardian.co.uk/document/d/1pqRpgIAAlcUMafbA3T7ZHg54kEs2E9XeANzRoNrwTrE/edit?usp=sharing"
        >
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

  render() {
    if (this.props.currentPath.endsWith('/upload')) {
      return (
        <header className="topbar flex-container">
          {this.renderProgress()}
          {this.renderHeaderBack()}
        </header>
      );
    }
    if (!this.props.showPublishedState) {
      return (
        <header className="topbar flex-container">
          {this.renderProgress()}

          {this.renderHome()}
          {this.renderSearch()}

          <div className="flex-spacer" />

          <div className="flex-container">
            {this.renderCreateVideo()}
          </div>

          <div className="flex-container">
            {this.renderFeedback()}
            {this.renderHowTo()}
          </div>

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

          <VideoPublishBar
            className="flex-grow"
            video={this.props.video}
            publishedVideo={this.props.publishedVideo}
            editableFields={this.props.editableFields}
            saveState={this.props.saveState}
            videoEditOpen={this.props.videoEditOpen}
            updateVideoPage={this.props.updateVideoPage}
            usages={this.props.usages}
            publishVideo={this.publishVideo}
          />

          <AdvancedActions video={this.props.video || {}} />
          <ComposerPageCreate
            usages={this.props.usages}
            videoEditOpen={this.props.videoEditOpen}
            video={this.props.video || {}}
            createVideoPage={this.props.createVideoPage}
          />
          <div className="flex-container">
            {this.renderAuditLink()}
          </div>

        </header>
      );
    }
  }
}
