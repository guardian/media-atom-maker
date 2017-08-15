import React from 'react';
import { ManagedForm, ManagedField } from '../ManagedForm';
import SelectBox from '../FormFields/SelectBox';

export default class WorkflowSectionPicker extends React.Component {
  render() {
    return (
      <div>
        <ManagedForm
          data={this.props.video}
          updateData={this.props.saveVideo}
          editable={true}
          formName="WorkflowSection"
        >
          <ManagedField fieldLocation="workflowSection" name="Workflow Section">
            <SelectBox selectValues={this.props.sections} />
          </ManagedField>
        </ManagedForm>
      </div>
    );
  }
}
