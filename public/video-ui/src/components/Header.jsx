import React from 'react';
import {Link} from 'react-router';
import VideoSearch from './VideoSearch/VideoSearch';
import VideoPublishBar from './VideoPublishBar/VideoPublishBar';
import VideoPublishState from './VideoPublishState/VideoPublishState';
import AdvancedActions from './Videos/AdvancedActions';
import ComposerPageCreate from './Videos/ComposerPageCreate';
import Icon from './Icon';
import {Presence} from './Presence';
import {canonicalVideoPageExists} from '../util/canonicalVideoPageExists';
import VideoUtils from '../util/video';
import {QUERY_PARAM_mediaPlatformFilter, QUERY_PARAM_shouldUseCreatedDateForSort} from "../constants/queryParams";
import Modal from "./utils/Modal";
import Create from "./Create";

export default class Header extends React.Component {
  state = {
    presence: null,
    createModalOpen: false,
  };

  closeCreateModal = () => {
    this.setState({ createModalOpen: false });
  };

  openCreateModal = () => {
    this.setState({ createModalOpen: true });
  };

  publishVideo = () => {
    this.props.publishVideo(this.props.video.id);
  };

  requiredComposerFieldsMissing = () => {
    return Object.keys(this.props.formFieldsWarning).some(key => {
      return this.props.formFieldsWarning[key];
    });
  };

  renderProgress() {
    if (this.props.s3Upload.status === 'uploading') {
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

  renderFilterBy() {
    return (
      <div className="flex-container topbar__global">
        <span>Filter for:&nbsp;</span>
        <select
          value={this.props.search.mediaPlatformFilter?.toString()}
          onChange={event => {
            const mediaPlatformFilter = event.target.value || null;

            this.props.updateMediaPlatformFilter(mediaPlatformFilter);

            const url = new URL(window.location.href);
            if (mediaPlatformFilter) {
              url.searchParams.set(QUERY_PARAM_mediaPlatformFilter, mediaPlatformFilter);
            } else {
              url.searchParams.delete(QUERY_PARAM_mediaPlatformFilter);
            }
            window.history.replaceState(
              window.history.state,
              document.title,
              url.toString()
            );
          }}
        >
          <option value="">All videos</option>
          <option value="url">Loops</option>
          <option value="youtube">YouTube</option>
        </select>
      </div>
    );
  }

  renderSortBy() {
    return (
      <div className="flex-container topbar__global">
        <span>Sort by:&nbsp;</span>
        <select
          value={this.props.search.shouldUseCreatedDateForSort?.toString()}
          onChange={event => {
            const shouldUseCreatedDateForSort = event.target.value === "true";

            this.props.updateShouldUseCreatedDateForSort(shouldUseCreatedDateForSort);

            const url = new URL(window.location.href);
            if (shouldUseCreatedDateForSort) {
              url.searchParams.set(QUERY_PARAM_shouldUseCreatedDateForSort, "true");
            } else {
              url.searchParams.delete(QUERY_PARAM_shouldUseCreatedDateForSort);
            }
            window.history.replaceState(
              window.history.state,
              document.title,
              url.toString()
            );
          }}
        >
          <option value="false">Last Modified (default)</option>
          <option value="true">Created (newest first)</option>
        </select>
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

  renderHelpLink() {
    return (
      <nav className="topbar__nav-link">
        <Link className="button__secondary" to="/help">
          <Icon icon="live_help">Help</Icon>
        </Link>
      </nav>
    );
  }

  renderCreateVideo() {
    return (
      <nav className="topbar__nav-link">
        <button className="btn" onClick={this.openCreateModal}>
          <Icon icon="add">Create</Icon>
        </button>
        <Modal isOpen={this.state.createModalOpen} dismiss={this.closeCreateModal}>
          <Create createVideo={this.props.createVideo} inModal={true} closeCreateModal={this.closeCreateModal}/>
        </Modal>
      </nav>
    );
  }

  renderPresence() {
    if (this.props.presenceConfig) {
      return (
        <Presence
          video={this.props.video}
          config={this.props.presenceConfig}
          reportPresenceClientError={this.props.reportPresenceClientError}
        />
      );
    }

    return false;
  }

  renderComposerMissingWarning() {
    if (!this.requiredComposerFieldsMissing()) {
      return null;
    }

    if (VideoUtils.isHosted(this.props.video)) {
      return (
        <div className="header__fields__missing__warning">
          Hosted video pages need a call to action. Please create this video in
          Composer
        </div>
      );
    }

    if (canonicalVideoPageExists(this.props.usages)) {
      return (
        <div className="header__fields__missing__warning">
          Fill in required composer fields before publishing
        </div>
      );
    }

    return (
      <div className="header__fields__missing__warning">
        Fill in required composer fields before creating video page
      </div>
    );
  }

  render() {
    const canHaveComposerPage =
      this.props.video.videoPlayerFormat !== 'Cinemagraph' &&
      this.props.video.videoPlayerFormat !== 'Loop';

    const className = this.props.isTrainingMode
      ? 'topbar topbar--training-mode flex-container'
      : 'topbar flex-container';

    if (this.props.currentPath.endsWith('/upload')) {
      return (
        <header className={className}>
          {this.renderProgress()}
          {this.renderHeaderBack()}
          {this.renderPresence()}
        </header>
      );
    }

    if (this.props.currentPath.endsWith('/create')) {
      return (
        <header className={className}>
          {this.renderHome()}
          <div className="flex-spacer" />
          {this.renderHelpLink()}
        </header>
      );
    }

    if (!this.props.showPublishedState) {
      return (
        <header className={className}>
          {this.renderProgress()}

          {this.renderHome()}
          {this.renderSearch()}
          {this.renderPresence()}

          <div className="flex-spacer" />

          {this.renderFilterBy()}
          {this.renderSortBy()}

          <div className="flex-container">
            {this.renderCreateVideo()}
            {this.renderHelpLink()}
          </div>
        </header>
      );
    } else {
      return (
        <header className={className}>
          {this.renderProgress()}
          {this.renderHome()}
          <VideoPublishState video={this.props.publishedVideo} />
          {this.renderPresence()}
          <div className="flex-spacer" />
          <VideoPublishBar
            className="flex-grow"
            video={this.props.video}
            isPublishing={this.props.isPublishing}
            publishedVideo={this.props.publishedVideo}
            videoEditOpen={this.props.videoEditOpen}
            updateVideoPage={this.props.updateVideoPage}
            requiredComposerFieldsMissing={this.requiredComposerFieldsMissing}
            usages={this.props.usages}
            publishVideo={this.publishVideo}
            formFieldsWarning={this.props.formFieldsWarning}
            updateVideo={this.props.updateVideo}
            saveVideo={this.props.saveVideo}
            query={this.props.query}
          />
          <AdvancedActions
            video={this.props.video || {}}
            usages={this.props.usages}
            deleteVideo={this.props.deleteVideo}
          />
          {canHaveComposerPage &&
            <>
              <ComposerPageCreate
                videoEditOpen={this.props.videoEditOpen}
                video={this.props.video || {}}
                createVideoPage={this.props.createVideoPage}
                requiredComposerFieldsMissing={this.requiredComposerFieldsMissing}
                usages={this.props.usages}
                error={this.props.error}
              />
              {this.renderComposerMissingWarning()}
            </>
          }
          <div className="flex-container">
            {this.renderHelpLink()}
          </div>

        </header>
      );
    }
  }
}
