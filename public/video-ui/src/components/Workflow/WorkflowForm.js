import React from 'react';
import PropTypes from 'prop-types';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import TextAreaInput from '../FormFields/TextAreaInput';

export default class WorkflowForm extends React.Component {
  static propTypes = {
    editable: PropTypes.bool.isRequired,
    video: PropTypes.object.isRequired,
    workflowSections: PropTypes.array.isRequired,
    workflowStatuses: PropTypes.array.isRequired,
    workflowPriorities: PropTypes.array.isRequired,
    workflowStatus: PropTypes.object.isRequired,
    workflowProductionOffices: PropTypes.array.isRequired,
    updateData: PropTypes.func.isRequired
  };

  render() {
    return (
      <ManagedForm
        data={this.props.workflowStatus}
        updateData={this.props.updateData}
        editable={this.props.editable}
        formName="WorkflowDetails"
      >
        <ManagedField
          fieldLocation="prodOffice"
          name="Production Office"
          disabled={!this.props.editable}
        >
          <SelectBox selectValues={this.props.workflowProductionOffices} />
        </ManagedField>
        <ManagedField
          fieldLocation="section"
          name="Section"
          disabled={!this.props.editable || this.props.workflowStatus.isTrackedInWorkflow}>
          <SelectBox selectValues={this.props.workflowSections} />
        </ManagedField>
        <ManagedField
          fieldLocation="note"
          name="Note"
          disabled={!this.props.editable}>
          <TextAreaInput />
        </ManagedField>
        <ManagedField
          fieldLocation="status"
          name="Status"
          disabled={!this.props.editable}>
          <SelectBox selectValues={this.props.workflowStatuses} />
        </ManagedField>
        <ManagedField
          fieldLocation="priority"
          name="Priority"
          disabled={!this.props.editable}>
          <SelectBox selectValues={this.props.workflowPriorities}
          />
        </ManagedField>
      </ManagedForm>
    );
  }
}


