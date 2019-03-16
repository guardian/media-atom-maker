import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import Flags from '../../../components/Flags';
import { formNames } from '../../../constants/formNames';
import EditSaveCancel from '../../../components/EditSaveCancel';
import VideoData from '../../../components/VideoData/VideoData';

export class FurnitureTab extends React.Component {
  static tabsRole = Tab.tabsRole;

  render() {
    return (
      <Tab {...this.props}>
        Furniture
      </Tab>
    );
  }
}

export class FurnitureTabPanel extends React.Component {
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
    updateWarnings: PropTypes.func.isRequired,
    usages: PropTypes.object.isRequired
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
      usages,
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

        <VideoData
          video={video}
          updateVideo={updateVideo}
          editable={editing}
          updateErrors={updateErrors}
          updateWarnings={updateWarnings}
          usages={usages}
        />

        <Flags
          video={video}
          updateVideo={updateVideo}
          editable={editing}
          formName={formNames.flags}
          updateErrors={updateErrors}
          updateWarnings={updateWarnings}
        />
      </TabPanel>
    );
  }
}
