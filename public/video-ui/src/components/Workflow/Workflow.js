import React from 'react';
import PropTypes from 'prop-types';

export default class Workflow extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  trackInWorkflow () {
    
  }

  renderTrackInWorkflowButton() {
    return (
      <button type="button"
              className="btn"
              onClick={this.trackInWorkflow}
      >
        Track in Workflow
      </button>
    )
  }

  render () {
    return (
      <div>
        {this.renderTrackInWorkflowButton()}
      </div>
    )
  }
}

