import React from 'react';
import VideosApi from '../../services/VideosApi';
import Icon from '../Icon';

class DurationReset extends React.Component {
  render() {
    const { video, updateVideo, editable } = this.props;

    if (!video || !video.id || editable) {
      return null;
    }

    return (
      <button
        title="Refresh video duration from active YouTube video"
        type="button"
        disabled={!video.hasOwnProperty('activeVersion')}
        data-tip="Refresh video duration from active YouTube video"
        onClick={() => {
          VideosApi.resetDurationFromActive(video.id).then(video => {
            updateVideo(video);
          });
        }}
      >
        <Icon
          icon="refresh"
          className="icon__edit"
          disabled={!video.hasOwnProperty('activeVersion')}
        />
      </button>
    );
  }
}

export default DurationReset;
