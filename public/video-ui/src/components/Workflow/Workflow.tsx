import React from 'react';
import WorkflowForm from './WorkflowForm';

type Props = {
  video: Video;
  editable: boolean;
};

class Workflow extends React.Component<Props> {
  state = {
    editing: false
  };

  hasSections = () => (this.props as any).workflow.sections.length > 0;
  hasStatuses = () => (this.props as any).workflow.statuses.length > 0;
  hasPriorities = () => (this.props as any).workflow.priorities.length > 0;

  componentDidMount() {
    if (!this.hasSections()) {
      (this.props as any).workflowActions.getSections();
    }

    if (!this.hasStatuses()) {
      (this.props as any).workflowActions.getStatuses();
    }

    if (!this.hasPriorities()) {
      (this.props as any).workflowActions.getPriorities();
    }

    (this.props as any).workflowActions.getStatus(this.props.video);
  }

  updateLocalData = (e: any) => {
    (this.props as any).workflowActions.localUpdateWorkflowData(e);
    return Promise.resolve(e);
  };

  render() {
    const { editable, video } = this.props;

    return (
      <WorkflowForm
        editable={editable}
        video={video}
        workflowSections={(this.props as any).workflow.sections || []}
        workflowStatuses={(this.props as any).workflow.statuses || []}
        workflowPriorities={
          (this.props as any).workflow.priorities.map(
            ({ name, value }: any) => ({
              id: value,
              title: name
            })
          ) || []
        }
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
