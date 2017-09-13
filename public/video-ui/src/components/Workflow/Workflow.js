import React from 'react';
import PropTypes from 'prop-types';
import { Workflow as WorkflowConstants } from '../../constants/workflow';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import WorkflowApi from '../../services/WorkflowApi';

class Workflow extends React.Component {
  static propTypes = {
    video: PropTypes.object.isRequired
  };

  state = {
    videoInWorkflow: {}
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
      video: this.props.video,
      section: this.props.workflow.sections.find(_ => _.id === parseInt(this.state.videoInWorkflow.section)),
      status: 'Writers'
    }).then(() => {
      this.props.workflowActions.getStatus({ video: this.props.video });
    });
  }

  updateWorkflowSection({ section }) {
    this.state.videoInWorkflow.section = section;
  }

  renderTrackInWorkflow() {
    return (
      <div>
        <ManagedForm data={this.state.videoInWorkflow}
                     updateData={(e) => this.updateWorkflowSection(e)}
                     editable={true}
                     formName="WorkflowSection">
          <ManagedField fieldLocation="section" name="Section">
            <SelectBox selectValues={this.props.workflow.sections} />
          </ManagedField>
        </ManagedForm>
        <button type="button"
                className="btn"
                onClick={() => this.trackInWorkflow()}>
          Track in Workflow
        </button>
      </div>
    );
  }

  renderStatusInWorkflow() {
    return (
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Production Office</th>
            <th>Section</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>{this.props.workflow.status.title}</td>
            <td>{this.props.workflow.status.prodOffice}</td>
            <td>{this.props.workflow.status.section}</td>
            <td>{this.props.workflow.status.status}</td>
            <td>
              <a target="_blank"
                 rel="noopener noreferrer"
                 href={WorkflowApi.workflowItemLink(this.props.video)}>
                Open in Workflow
              </a>
            </td>
          </tr>
        </tbody>
      </table>
    );
  }

  render () {
    if (this.props.workflow.status === WorkflowConstants.notInWorkflow) {
      return (
        <div className="form__group">
          {this.renderTrackInWorkflow()}
        </div>
      );
    }

    return (
      <div className="form__group">
        {this.renderStatusInWorkflow()}
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
