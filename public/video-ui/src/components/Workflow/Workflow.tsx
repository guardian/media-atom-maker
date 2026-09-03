import React from 'react';
import { WorkflowForm } from './WorkflowForm';

type Props = {
  video: Video;
  editable: boolean;
};

class Workflow extends React.Component<Props> {
  state = {
    editing: false
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  hasSections = () => (this.props as any).workflow.sections.length > 0;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  hasStatuses = () => (this.props as any).workflow.statuses.length > 0;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  hasPriorities = () => (this.props as any).workflow.priorities.length > 0;

  componentDidMount() {
    if (!this.hasSections()) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (this.props as any).workflowActions.getSections();
    }

    if (!this.hasStatuses()) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (this.props as any).workflowActions.getStatuses();
    }

    if (!this.hasPriorities()) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (this.props as any).workflowActions.getPriorities();
    }

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (this.props as any).workflowActions.getStatus(this.props.video);
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateLocalData = (e: any) => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    (this.props as any).workflowActions.localUpdateWorkflowData(e);
    return Promise.resolve(e);
  };

  render() {
    const { editable, video } = this.props;

    return (
      <WorkflowForm
        editable={editable}
        video={video}
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        workflowSections={(this.props as any).workflow.sections || []}
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        workflowStatuses={(this.props as any).workflow.statuses || []}
        workflowPriorities={
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (this.props as any).workflow.priorities.map(
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            ({ name, value }: any) => ({
              id: value,
              title: name
            })
          ) || []
        }
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        workflowStatus={(this.props as any).workflow.status}
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
import { AnyAction, bindActionCreators, Dispatch } from 'redux';
import {
  getPriorities,
  getSections,
  getStatus,
  getStatuses,
  localUpdateWorkflowData
} from '../../slices/workflow';
import { Video } from '../../services/VideosApi';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function mapStateToProps(state: { workflow: any }) {
  return {
    workflow: state.workflow
  };
}

function mapDispatchToProps(dispatch: Dispatch<AnyAction>) {
  return {
    workflowActions: bindActionCreators(
      {
        getStatus,
        getSections,
        getStatuses,
        getPriorities,
        localUpdateWorkflowData
      },
      dispatch
    )
  };
}

export default connect(mapStateToProps, mapDispatchToProps)(Workflow);
