import React from 'react';
import PropTypes from 'prop-types';
import WorkflowForm from './WorkflowForm';

class Workflow extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired,
    editable: PropTypes.bool.isRequired
  };

  state = {
    editing: false
  };

  hasSections = () => this.props.workflow.sections.length > 0;
  hasStatuses = () => this.props.workflow.statuses.length > 0;
  hasPriorities = () => this.props.workflow.priorities.length > 0;

  componentDidMount() {
    if (!this.hasSections()) {
      this.props.workflowActions.getSections();
    }

    if (!this.hasStatuses()) {
      this.props.workflowActions.getStatuses();
    }

    if (!this.hasPriorities()) {
      this.props.workflowActions.getPriorities();
    }

    this.props.workflowActions.getStatus(this.props.video);
  }

  updateLocalData = e => {
    this.props.workflowActions.localUpdateWorkflowData(e);
    return Promise.resolve(e);
  };

  render() {
    const { editable, video } = this.props;

    return (
      <WorkflowForm
        editable={editable}
        video={video}
        workflowSections={this.props.workflow.sections || []}
        workflowStatuses={this.props.workflow.statuses || []}
        workflowPriorities={
          this.props.workflow.priorities.map(({ name, value }) => ({ id: value, title: name })) || []
        }
        workflowStatus={this.props.workflow.status}
        workflowProductionOffices={[
          { id: 'UK', title: 'UK' },
          { id: 'US', title: 'US' },
          { id: 'AU', title: 'AU' }
        ]}
        updateData={this.updateLocalData}
      />
    );
  }
}

import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import { getStatus, getSections } from '../../slices/workflow';
import * as getStatuses from '../../actions/WorkflowActions/getStatuses';
import * as getPriorities from '../../actions/WorkflowActions/getPriorities';
import * as localUpdateWorkflowData from '../../actions/WorkflowActions/localUpdateWorkflowData';

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
        getStatus,
        getSections,
        getStatuses,
        getPriorities,
        localUpdateWorkflowData
      ),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Workflow);
