import React from 'react';
import { Tab, TabPanel } from 'react-tabs';
import ContentChangeDetails from '../../../components/ContentChangeDetails';
import DurationReset from '../../../components/DurationReset/DurationReset';
import type { Video } from '../../../services/VideosApi';

type ManagementTabPanelProps = Omit<
  React.ComponentProps<typeof TabPanel>,
  'children'
> & {
  video: Video;
  updateVideo: (video?: Video) => void;
};

export const ManagementTab = (props: React.ComponentProps<typeof Tab>) => (
  <Tab {...props}>Management</Tab>
);

ManagementTab.tabsRole = Tab.tabsRole;

export const ManagementTabPanel = ({
  video,
  updateVideo,
  ...rest
}: ManagementTabPanelProps) => (
  <TabPanel {...rest}>
    <div className="form__group">
      <header className="video__detailbox__header">Content Details</header>
      <ContentChangeDetails video={video} />
    </div>

    <div className="form__group">
      <header className="video__detailbox__header">Duration Reset</header>
      <DurationReset video={video} updateVideo={updateVideo} />
    </div>
  </TabPanel>
);

ManagementTabPanel.tabsRole = TabPanel.tabsRole;
