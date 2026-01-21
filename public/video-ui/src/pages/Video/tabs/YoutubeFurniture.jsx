import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import EditSaveCancel from '../../../components/EditSaveCancel';
import YoutubeFurniture from '../../../components/YoutubeFurniture';

export class YoutubeFurnitureTab extends React.Component {
  static tabsRole = Tab.tabsRole;

  render() {
    return (
      <Tab {...this.props}>
        YouTube Furniture
      </Tab>
    );
  }
}

export class YoutubeFurnitureTabPanel extends React.Component {
  static tabsRole = TabPanel.tabsRole;

  static propTypes = {
    editing: PropTypes.bool.isRequired,
    onEdit: PropTypes.func.isRequired,
    onSave: PropTypes.func.isRequired,
    onCancel: PropTypes.func.isRequired,
    canSave: PropTypes.func.isRequired,
    canCancel: PropTypes.func.isRequired,
    video: PropTypes.object.isRequired,
    updateVideo: PropTypes.func.isRequired,
    updateErrors: PropTypes.func.isRequired,
    updateWarnings: PropTypes.func.isRequired
  };

  render() {
    const {
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
      canCancel, // pulling out so it isn't passed to TabPanel, which causes an error
      ...rest
    } = this.props;

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
  }
}
