import React from 'react';
import PropTypes from 'prop-types';
import { isVideoPublished, hasVideoExpired } from '../../util/isVideoPublished';

export default class VideoPublishState extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  render() {
    if (hasVideoExpired(this.props.video)) {
      return <div className="publish__label label__expired">Expired</div>;
    }

    if (isVideoPublished(this.props.video)) {
      return <div className="publish__label label__live">Live</div>;
    }

    return <div className="publish__label label__draft">Draft</div>;
  }
}
