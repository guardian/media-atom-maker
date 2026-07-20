import React from 'react';
import VideosApi from '../../services/VideosApi';
import type { Video } from '../../services/VideosApi';
import Icon from '../Icon';

type DurationResetProps = {
  video?: Video;
  updateVideo: (video: Video) => void;
};

class DurationReset extends React.Component<DurationResetProps> {

  render() {
    const { video, updateVideo } = this.props;

    if (!video || !video.id) {
      return null;
    }

    return (
      <button
        title="Refresh video duration from active YouTube video"
        type="button"
        disabled={!video.hasOwnProperty('activeVersion')}
        data-tip="Refresh video duration from active YouTube video"
        onClick={() => {
          VideosApi.resetDurationFromActive(video.id).then(updatedVideo => {
            updateVideo(updatedVideo);
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
