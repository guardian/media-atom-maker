
import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import VideoUsages from '../../../components/VideoUsages/VideoUsages';

export class UsageTab extends React.Component {
  static tabsRole = Tab.tabsRole;

  render() {
    return (
      <Tab {...this.props}>
        Usages
      </Tab>
    );
  }
}

export class UsageTabPanel extends React.Component {
  static tabsRole = TabPanel.tabsRole;

  static propTypes = {
    video: PropTypes.object.isRequired,
    publishedVideo: PropTypes.object.isRequired,
    usages: PropTypes.object.isRequired
  };

  render() {
    const { video, publishedVideo, usages, ...rest } = this.props;

    return (
      <TabPanel {...rest}>
        <VideoUsages
          video={video}
          publishedVideo={publishedVideo}
          usages={usages}
        />
      </TabPanel>
    );
  }
}
