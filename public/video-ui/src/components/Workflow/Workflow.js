import React from 'react';
import PropTypes from 'prop-types';
import WorkflowSectionPicker from './WorkflowSectionPicker';
import { Workflow as WorkflowConstants } from '../../constants/workflow';

class Workflow extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  hasSections = () => this.props.workflow.sections.length !== 0;

  componentWillMount() {
    if (!this.hasSections()) {
      this.props.workflowActions.getSections();
    }

    this.props.workflowActions.getStatus({ video: this.props.video });
  }

  trackInWorkflow() {
    this.props.workflowActions.trackInWorkflow({
      video: this.props.video
    });
  }

  renderTrackInWorkflowButton() {
    if (this.props.workflow.status === WorkflowConstants.notInWorkflow) {
      return (
        <button type="button"
                className="btn"
                onClick={this.trackInWorkflow}
        >
          Track in Workflow
        </button>
      );
    }
  }

  render () {
    return (
      <div>
        {this.renderTrackInWorkflowButton()}
        <WorkflowSectionPicker sections={this.props.workflow.sections}
                               saveVideo={_ => console.log(_)}/>
      </div>
    );
  }
}

//REDUX CONNECTIONS
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getSections from '../../actions/WorkflowActions/getSections';
import * as getStatus from '../../actions/WorkflowActions/getStatus';
import * as trackInWorkflow from '../../actions/WorkflowActions/trackInWorkflow';

function mapStateToProps(state) {
  return {
    workflow: state.workflow
  };
}

function mapDispatchToProps(dispatch) {
  return {
    workflowActions: bindActionCreators(
      Object.assign(
        {},
        getSections,
        getStatus,
        trackInWorkflow
      ),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Workflow);
