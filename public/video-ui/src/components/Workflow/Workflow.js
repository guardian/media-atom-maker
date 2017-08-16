import React from 'react';
import PropTypes from 'prop-types';
import WorkflowSectionPicker from './WorkflowSectionPicker';

export default class Workflow extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    status: PropTypes.object.isRequired,
    sections: PropTypes.array.isRequired
  };

  trackInWorkflow () {
    return true;
  }

  renderTrackInWorkflowButton() {
    return (
      <button type="button"
              className="btn"
              onClick={this.trackInWorkflow}
      >
        Track in Workflow
      </button>
    );
  }

  render () {
    return (
      <div>
        {this.renderTrackInWorkflowButton()}
        <WorkflowSectionPicker sections={this.props.sections}
                               saveVideo={_ => console.log(_)}/>
      </div>
    );
  }
}

