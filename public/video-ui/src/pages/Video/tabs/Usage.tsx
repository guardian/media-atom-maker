import React from 'react';
import { Tab, TabPanel } from 'react-tabs';
import VideoUsages from '../../../components/VideoUsages/VideoUsages';
import type { Video } from '../../../services/VideosApi';
import type { UsageState } from '../../../slices/usage';

type UsageTabPanelProps = Omit<
  React.ComponentProps<typeof TabPanel>,
  'children'
> & {
  video: Video;
  publishedVideo: Video;
  usages: UsageState;
};

export const UsageTab = (props: React.ComponentProps<typeof Tab>) => (
  <Tab {...props}>Usages</Tab>
);

UsageTab.tabsRole = Tab.tabsRole;

export const UsageTabPanel = ({
  video,
  publishedVideo,
  usages,
  ...rest
}: UsageTabPanelProps) => (
  <TabPanel {...rest}>
    <VideoUsages
      video={video}
      publishedVideo={publishedVideo}
      usages={usages}
    />
  </TabPanel>
);

UsageTabPanel.tabsRole = TabPanel.tabsRole;
