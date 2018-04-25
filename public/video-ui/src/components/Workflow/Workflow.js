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

  componentWillMount() {
    if (!this.hasSections()) {
      this.props.workflowActions.getSections();
    }

    if (!this.hasStatuses()) {
      this.props.workflowActions.getStatuses();
    }

    this.props.workflowActions.getStatus(this.props.video);
  }

  updateLocalData = e => {
    this.props.workflowActions.localUpdateWorkflowStatus(e);
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

  updateStatus() {
    return this.props.workflowActions.updateWorkflowStatus({
      workflowItem: this.props.workflow.status
    })
  }

  saveInWorkflow() {
    const wfPromise = this.props.workflow.status.isTrackedInWorkflow
      ? this.updateStatus()
      : this.sendToWorkflow();

    wfPromise.then(() => {
      this.props.workflowActions.getStatus(this.props.video);
    });
  }

  manageEditingState({ editing, save = false }) {
    if (save) {
      this.saveInWorkflow();
    } else {
      this.props.workflowActions.getStatus(this.props.video);
    }

    this.setState({ editing: editing });
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

  renderFormButtons() {
    if (!this.state.editing) {
      return (
        <span>
          {this.renderViewInWorkflowLink()}
          <button onClick={() => this.manageEditingState({editing: true})}>
            <Icon icon="edit" className="icon__edit"/>
          </button>
        </span>
      );
    } else {
      const canSave = this.props.workflow.status.section && this.props.workflow.status.status;

      return (
        <span>
          <button
            onClick={() => this.manageEditingState({editing: false, save: true})}
            disabled={!canSave}
          >
            <Icon icon="save" className={`icon__done ${canSave ? '' : 'disabled'}`}>
              Save changes
            </Icon>
          </button>
          <button onClick={() => this.manageEditingState({editing: false})}>
            <Icon icon="cancel" className="icon__cancel">Cancel</Icon>
          </button>
        </span>
      );
    }
  }

  render() {
    return (
      <div className="video__detailbox">
        <div className="video__detailbox__header__container">
          <header className="video__detailbox__header">Workflow</header>
          {this.renderFormButtons()}
        </div>
        <div className="form__group">
          <WorkflowForm
            editable={this.state.editing}
            video={this.props.video || {}}
            workflowSections={this.props.workflow.sections || []}
            workflowStatuses={this.props.workflow.statuses || []}
            workflowStatus={this.props.workflow.status}
            updateData={this.updateLocalData}
          />
        </div>
      </div>
    );
  }
}

import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as getStatus from '../../actions/WorkflowActions/getStatus';
import * as getSections from '../../actions/WorkflowActions/getSections';
import * as getStatuses from '../../actions/WorkflowActions/getStatuses';
import * as trackInWorkflow from '../../actions/WorkflowActions/trackInWorkflow';
import * as updateWorkflowStatus from '../../actions/WorkflowActions/updateWorkflowStatus';
import * as localUpdateWorkflowStatus from '../../actions/WorkflowActions/localUpdateWorkflowStatus';
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
        trackInWorkflow,
        updateWorkflowStatus,
        localUpdateWorkflowStatus
      ),
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Workflow);
