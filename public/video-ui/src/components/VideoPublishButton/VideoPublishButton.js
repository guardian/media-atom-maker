import React from 'react';

export default class VideoPublishButton extends React.Component {

  render() {
    return (
      <button type="button" className="btn" onClick={this.props.publishVideo}>Publish</button>
    )
  }
}
