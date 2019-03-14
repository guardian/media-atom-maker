import React from 'react';
import PropTypes from 'prop-types';
import WorkflowForm from './WorkflowForm';
import Icon from '../Icon';

class Workflow extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  state = {
    editing: false
  };

  hasSections = () => this.props.workflow.sections.length > 0;
  hasStatuses = () => this.props.workflow.statuses.length > 0;
  hasPriorities = () => this.props.workflow.priorities.length > 0;

  componentWillMount() {
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

  sendToWorkflow() {
    return this.props.workflowActions.trackInWorkflow({
      video: this.props.video,
      section: this.props.workflow.sections.find(_ => _.id === this.props.workflow.status.section),
      status: this.props.workflow.status.status,
      note: this.props.workflow.status.note
    });
  }

  updateData() {
    return this.props.workflowActions.updateWorkflowData({
      workflowItem: this.props.workflow.status
    })
  }

  saveInWorkflow() {
    const wfPromise = this.props.workflow.status.isTrackedInWorkflow
      ? this.updateData()
      : this.sendToWorkflow();

    wfPromise.then(() => {
      this.props.workflowActions.getStatus(this.props.video);
    });
  }

  renderViewInWorkflowLink() {
    if (this.props.workflow.status.isTrackedInWorkflow) {
      return (
        <a className="button inline-block"
           target="_blank"
           rel="noopener noreferrer"
           href={WorkflowApi.workflowItemLink(this.props.video)}>
          <Icon icon="open_in_new" className="icon__edit"/>
        </a>
      );
    }
  }

  render() {
    return (
      <div>
        {this.renderViewInWorkflowLink()}
        <WorkflowForm
          editable={this.props.editable}
          video={this.props.video || {}}
          workflowSections={this.props.workflow.sections || []}
          workflowStatuses={this.props.workflow.statuses || []}
          workflowPriorities={this.props.workflow.priorities}
          workflowStatus={this.props.workflow.status}
          updateData={this.updateLocalData}
        />
      </div>
    );
  }
}

import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getStatus from '../../actions/WorkflowActions/getStatus';
import * as getSections from '../../actions/WorkflowActions/getSections';
import * as getStatuses from '../../actions/WorkflowActions/getStatuses';
import * as getPriorities from '../../actions/WorkflowActions/getPriorities';
import * as trackInWorkflow from '../../actions/WorkflowActions/trackInWorkflow';
import * as updateWorkflowData from '../../actions/WorkflowActions/updateWorkflowData';
import * as localUpdateWorkflowData from '../../actions/WorkflowActions/localUpdateWorkflowData';
import WorkflowApi from "../../services/WorkflowApi";

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
        trackInWorkflow,
        updateWorkflowData,
        localUpdateWorkflowData
      ),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Workflow);
