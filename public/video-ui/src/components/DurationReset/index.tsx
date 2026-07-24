import React from 'react';
import VideosApi, { Video } from '../../services/VideosApi';
import Icon from '../Icon';

type Props = {
  video: Video;
  updateVideo: (...args: any[]) => any;
};

class DurationReset extends React.Component<Props> {
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
