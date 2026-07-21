import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';
import TextAreaInput from '../FormFields/TextAreaInput';
import { Video } from '../../services/VideosApi';

type Props = {
  editable: boolean;
  video: Video;
  workflowSections: any[];
  workflowStatuses: any[];
  workflowPriorities: any[];
  workflowStatus: any;
  workflowProductionOffices: any[];
  updateData: (...args: any[]) => any;
};

export default class WorkflowForm extends React.Component<Props> {
  render() {
    return (
      // @ts-expect-error TS(2769): No overload matches this call.
      <ManagedForm
        data={this.props.workflowStatus}
        updateData={this.props.updateData}
        editable={this.props.editable}
        formName="WorkflowDetails"
      >
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="prodOffice"
          name="Production Office"
          disabled={!this.props.editable}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <SelectBox selectValues={this.props.workflowProductionOffices} />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="section"
          name="Section"
          disabled={
            !this.props.editable ||
            this.props.workflowStatus.isTrackedInWorkflow
          }
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <SelectBox selectValues={this.props.workflowSections} />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="note"
          name="Note"
          disabled={!this.props.editable}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <TextAreaInput />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="status"
          name="Status"
          disabled={!this.props.editable}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <SelectBox selectValues={this.props.workflowStatuses} />
        </ManagedField>
        {/* @ts-expect-error TS(2769): No overload matches this call. */}
        <ManagedField
          fieldLocation="priority"
          name="Priority"
          disabled={!this.props.editable}
        >
          {/* @ts-expect-error TS(2769): No overload matches this call. */}
          <SelectBox selectValues={this.props.workflowPriorities} />
        </ManagedField>
      </ManagedForm>
    );
  }
}
