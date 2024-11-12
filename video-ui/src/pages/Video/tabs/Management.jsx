import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import ContentChangeDetails from '../../../components/ContentChangeDetails';
import DurationReset from '../../../components/DurationReset';

export class ManagementTab extends React.Component {
  static tabsRole = Tab.tabsRole;

  render() {
    return (
      <Tab {...this.props}>
        Management
      </Tab>
    );
  }
}

export class ManagementTabPanel extends React.Component {
  static tabsRole = TabPanel.tabsRole;

  static propTypes = {
    video: PropTypes.object.isRequired,
    updateVideo: PropTypes.func.isRequired
  };

  render() {
    const { video, updateVideo, ...rest } = this.props;

    return (
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
  }
}
