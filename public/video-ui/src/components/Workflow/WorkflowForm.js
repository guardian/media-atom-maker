import React from 'react';
import PropTypes from 'prop-types';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import TextInput from '../FormFields/TextInput';

export default class WorkflowForm extends React.Component {
  static propTypes = {
    editable: PropTypes.bool.isRequired,
    video: PropTypes.object.isRequired,
    workflowSections: PropTypes.array.isRequired,
    workflowStatuses: PropTypes.array.isRequired,
    workflowStatus: PropTypes.object.isRequired,
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
          disabled={true}
        >
          <TextInput />
        </ManagedField>
        <ManagedField
          fieldLocation="section"
          name="Section"
          disabled={!this.props.editable || this.props.workflowStatus.isTrackedInWorkflow}>
          <SelectBox selectValues={this.props.workflowSections} />
        </ManagedField>
        <ManagedField
          fieldLocation="status"
          name="Status"
          disabled={!this.props.editable}>
          <SelectBox selectValues={this.props.workflowStatuses} />
        </ManagedField>
      </ManagedForm>
    );
  }
}