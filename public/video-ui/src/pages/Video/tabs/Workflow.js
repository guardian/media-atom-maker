import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import EditSaveCancel from '../../../components/EditSaveCancel';
import Workflow from '../../../components/Workflow/Workflow';

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
    video: PropTypes.object.isRequired,
    workflow: PropTypes.object.isRequired
  };

  render() {
    const {
      editing,
      onEdit,
      onSave,
      onCancel,
      video,
      workflow,
      ...rest
    } = this.props;

    const canSave = workflow.status.section && workflow.status.status;

    return (
      <TabPanel {...rest}>
        <EditSaveCancel
          editing={editing}
          onEdit={onEdit}
          onSave={onSave}
          onCancel={onCancel}
          canSave={() => canSave || false}
          canCancel={() => true}
        />
        <Workflow video={video} editable={editing} />
      </TabPanel>
    );
  }
}
