import React from 'react';
import { Tab, TabPanel } from 'react-tabs';
import { EditSaveCancel } from '../../../components/EditSaveCancel';
import YoutubeFurniture from '../../../components/YoutubeFurniture';
import type { Video } from '../../../services/VideosApi';
import type {
  UpdateErrors,
  UpdateWarnings
} from '../../../components/ManagedForm/types';

type YoutubeFurnitureTabPanelProps = Omit<
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
};

export const YoutubeFurnitureTab = (
  props: React.ComponentProps<typeof Tab>
) => <Tab {...props}>YouTube Furniture</Tab>;

YoutubeFurnitureTab.tabsRole = Tab.tabsRole;

export const YoutubeFurnitureTabPanel = ({
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
  ...rest
}: YoutubeFurnitureTabPanelProps) => {
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
      <YoutubeFurniture
        video={video}
        editable={editing}
        updateVideo={updateVideo}
        updateErrors={updateErrors}
        updateWarnings={updateWarnings}
      />
    </TabPanel>
  );
};

YoutubeFurnitureTabPanel.tabsRole = TabPanel.tabsRole;
