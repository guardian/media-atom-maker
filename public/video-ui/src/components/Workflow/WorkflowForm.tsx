import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import TextAreaInput from '../FormFields/TextAreaInput';
import type { Video } from '../../services/VideosApi';
import type { WorkflowState } from '../../slices/workflow';

type WorkflowStatusFormData = WorkflowState['status'];
type WorkflowSectionSelectValue = WorkflowState['sections'][number];
type WorkflowStatusSelectValue = WorkflowState['statuses'][number];
type WorkflowPrioritySelectValue = {
  id: WorkflowState['priorities'][number]['value'];
  title: WorkflowState['priorities'][number]['name'];
};
type WorkflowProductionOfficeSelectValue = {
  id: string;
  title: string;
};

type Props = {
  editable: boolean;
  video: Video;
  workflowSections: WorkflowSectionSelectValue[];
  workflowStatuses: WorkflowStatusSelectValue[];
  workflowPriorities: WorkflowPrioritySelectValue[];
  workflowStatus: WorkflowStatusFormData;
  workflowProductionOffices: WorkflowProductionOfficeSelectValue[];
  updateData: (data: WorkflowStatusFormData) => Promise<WorkflowStatusFormData>;
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
