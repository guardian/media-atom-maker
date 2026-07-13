import React from 'react';
import { isVideoPublished, hasVideoExpired } from '../../util/isVideoPublished';
import type { Video } from '../../services/VideosApi';

type Props = {
  video: Video;
};

export default class VideoPublishState extends React.Component<Props> {
  render() {
    if (hasVideoExpired(this.props.video)) {
      return <div className="publish__label label__expired">Expired</div>;
    }

    if (isVideoPublished(this.props.video)) {
      return <div className="publish__label label__live">Published</div>;
    }

    return <div className="publish__label label__draft">Draft</div>;
  }
}