import React from 'react';
import { Tab, TabPanel } from 'react-tabs';
import { EditSaveCancel } from '../../../components/EditSaveCancel';
import Workflow from '../../../components/Workflow/Workflow';
import WorkflowLink from '../../../components/Workflow/WorkflowLink';
import type { Video } from '../../../services/VideosApi';

type TabComponent = ((
  props: React.ComponentProps<typeof Tab>
) => JSX.Element) & {
  tabsRole?: string;
};

type WorkflowTabPanelProps = Omit<
  React.ComponentProps<typeof TabPanel>,
  'children'
> & {
  editing: boolean;
  onEdit: () => void;
  onSave: () => void;
  onCancel: () => void;
  canSave: boolean;
  canCancel: boolean;
  video: Video;
  isTrackedInWorkflow: boolean;
};

export const WorkflowTab = (props: React.ComponentProps<typeof Tab>) => (
  <Tab {...props}>Workflow</Tab>
);

WorkflowTab.tabsRole = Tab.tabsRole;

export const WorkflowTabPanel = ({
  editing,
  onEdit,
  onSave,
  onCancel,
  canSave,
  canCancel,
  video,
  isTrackedInWorkflow,
  ...rest
}: WorkflowTabPanelProps) => {
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
};

WorkflowTabPanel.tabsRole = TabPanel.tabsRole;
