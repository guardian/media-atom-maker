import React from 'react';
import { Tab, TabPanel } from 'react-tabs';
import { EditSaveCancel } from '../../../components/EditSaveCancel';
import VideoData from '../../../components/VideoData/VideoData';
import Flags from '../../../components/Flags';
import type { Video } from '../../../services/VideosApi';
import type {
  UpdateErrors,
  UpdateWarnings
} from '../../../components/ManagedForm/types';

type FurnitureTabPanelProps = Omit<
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
  updateVideo: (video?: Video | undefined) => void;
  updateErrors: UpdateErrors;
  updateWarnings: UpdateWarnings;
  canonicalVideoPageExists: boolean;
};

export const FurnitureTab = (props: React.ComponentProps<typeof Tab>) => (
  <Tab {...props}>Furniture</Tab>
);

FurnitureTab.tabsRole = Tab.tabsRole;

export const FurnitureTabPanel = ({
  editing,
  onEdit,
  onSave,
  onCancel,
  canSave,
  canCancel,
  video,
  updateVideo,
  updateErrors,
  updateWarnings,
  canonicalVideoPageExists,
  ...rest
}: FurnitureTabPanelProps) => {
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

      <VideoData
        video={video}
        updateVideo={updateVideo}
        editable={editing}
        updateErrors={updateErrors}
        updateWarnings={updateWarnings}
        canonicalVideoPageExists={canonicalVideoPageExists}
      />

      <Flags
        video={video}
        editable={editing}
        updateVideo={updateVideo}
        updateErrors={updateErrors}
        updateWarnings={updateWarnings}
      />
    </TabPanel>
  );
};

FurnitureTabPanel.tabsRole = TabPanel.tabsRole;
