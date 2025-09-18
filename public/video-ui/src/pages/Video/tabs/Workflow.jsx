import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import EditSaveCancel from '../../../components/EditSaveCancel';
import Workflow from '../../../components/Workflow/Workflow';
import WorkflowLink from '../../../components/Workflow/WorkflowLink';

export class WorkflowTab extends React.Component {
  static tabsRole = Tab.tabsRole;

  render() {
    return (
      <Tab {...this.props}>
        Workflow
      </Tab>
    );
  }
}

export class WorkflowTabPanel extends React.Component {
  static tabsRole = TabPanel.tabsRole;

  static propTypes = {
    editing: PropTypes.bool.isRequired,
    onEdit: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
    canSave: PropTypes.func.isRequired,
    video: PropTypes.object.isRequired,
    isTrackedInWorkflow: PropTypes.bool.isRequired
  };

  render() {
    const {
      editing,
      onEdit,
      onSave,
      onCancel,
      canSave,
      canCancel,
      video,
      isTrackedInWorkflow,
      ...rest
    } = this.props;

    return (
      <TabPanel {...rest}>
        <EditSaveCancel
          editing={editing}
          onEdit={onEdit}
          onSave={onSave}
          onCancel={onCancel}
          canSave={canSave}
          canCancel={canCancel}
        />
        {isTrackedInWorkflow && !editing && <WorkflowLink video={video} />}
        <Workflow video={video} editable={editing} />
      </TabPanel>
    );
  }
}
