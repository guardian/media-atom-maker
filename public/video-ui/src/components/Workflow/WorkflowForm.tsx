import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import TextAreaInput from '../FormFields/TextAreaInput';
import type { Video } from '../../services/VideosApi';
import type { WorkflowState } from '../../slices/workflow';

type SelectValue = {
  id: string | number;
  title: string;
};

type WorkflowStatusFormData = WorkflowState['status'];

type Props = {
  editable: boolean;
  video: Video;
  workflowSections: SelectValue[];
  workflowStatuses: SelectValue[];
  workflowPriorities: SelectValue[];
  workflowStatus: WorkflowStatusFormData;
  workflowProductionOffices: SelectValue[];
  updateData: (
    data: WorkflowStatusFormData
  ) => Promise<WorkflowStatusFormData> | unknown;
};

export class WorkflowForm extends React.Component<Props> {
  render() {
    const isTrackedInWorkflow =
      'isTrackedInWorkflow' in this.props.workflowStatus &&
      this.props.workflowStatus.isTrackedInWorkflow;

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
          disabled={!this.props.editable || isTrackedInWorkflow}
        >
          <SelectBox selectValues={this.props.workflowSections} />
        </ManagedField>
        <ManagedField
          fieldLocation="note"
          name="Note"
          disabled={!this.props.editable}
        >
          <TextAreaInput />
        </ManagedField>
        <ManagedField
          fieldLocation="status"
          name="Status"
          disabled={!this.props.editable}
        >
          <SelectBox selectValues={this.props.workflowStatuses} />
        </ManagedField>
        <ManagedField
          fieldLocation="priority"
          name="Priority"
          disabled={!this.props.editable}
        >
          <SelectBox selectValues={this.props.workflowPriorities} />
        </ManagedField>
      </ManagedForm>
    );
  }
}
