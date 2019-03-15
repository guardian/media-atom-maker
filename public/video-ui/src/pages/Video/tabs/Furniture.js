import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import Flags from '../../../components/Flags';
import { formNames } from '../../../constants/formNames';
import EditSaveCancel from '../../../components/EditSaveCancel';
import VideoData from '../../../components/VideoData/VideoData';
import FieldNotification from '../../../constants/FieldNotification';

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
    video: PropTypes.object.isRequired,
    updateVideo: PropTypes.func.isRequired,
    updateErrors: PropTypes.func.isRequired,
    updateWarnings: PropTypes.func.isRequired,
    canonicalVideoPageExists: PropTypes.bool.isRequired
  };

  validateKeywords = keywords => {
    if (
      !Array.isArray(keywords) ||
      keywords.length === 0 ||
      keywords.every(keyword => keyword.match(/^tone/))
    ) {
      if (this.props.canonicalVideoPageExists) {
        return new FieldNotification(
          'error',
          'A series or a keyword tag is required for updating composer pages',
          FieldNotification.error
        );
      }
      return new FieldNotification(
        'desired',
        'A series or a keyword tag is required for creating composer pages',
        FieldNotification.warning
      );
    }
    return null;
  };

  render() {
    const {
      editing,
      onEdit,
      onSave,
      onCancel,
      video,
      updateVideo,
      updateErrors,
      updateWarnings,
      canonicalVideoPageExists,
      ...rest
    } = this.props;

    return (
      <TabPanel {...rest}>
        <EditSaveCancel
          onEdit={onEdit}
          onSave={onSave}
          onCancel={onCancel}
          canSave={() => true}
        />

        <VideoData
          video={video}
          updateVideo={updateVideo}
          editable={editing}
          formName={formNames.videoData}
          updateErrors={updateErrors}
          updateWarnings={updateWarnings}
          validateKeywords={this.validateKeywords}
          canonicalVideoPageExists={canonicalVideoPageExists}
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
