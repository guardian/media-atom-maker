import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import Targeting from '../../../components/Targeting/Targeting';

export class TargetingTab extends React.Component {
  static tabsRole = Tab.tabsRole;

  render() {
    return (
      <Tab {...this.props}>
        Targeting
      </Tab>
    );
  }
}

export class TargetingTabPanel extends React.Component {
  static tabsRole = TabPanel.tabsRole;

  static propTypes = {
    video: PropTypes.object.isRequired
  };

  render() {
    const { video, ...rest } = this.props;

    return (
      <TabPanel {...rest}>
        <Targeting video={video} />
      </TabPanel>
    );
  }
}
