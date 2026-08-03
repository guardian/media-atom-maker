import React from 'react';
import { Tab, TabPanel } from 'react-tabs';
import Targeting from '../../../components/Targeting/Targeting';
import type { Video } from '../../../services/VideosApi';

type TargetingTabPanelProps = Omit<
  React.ComponentProps<typeof TabPanel>,
  'children'
> & {
  video: Video;
};

export const TargetingTab = (props: React.ComponentProps<typeof Tab>) => (
  <Tab {...props}>Targeting</Tab>
);

TargetingTab.tabsRole = Tab.tabsRole;

export const TargetingTabPanel = ({
  video,
  ...rest
}: TargetingTabPanelProps) => (
  <TabPanel {...rest}>
    <Targeting video={video} />
  </TabPanel>
);

TargetingTabPanel.tabsRole = TabPanel.tabsRole;
