import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import EditSaveCancel from "../../../components/EditSaveCancel";
import YoutubeFuniture from '../../../components/YoutubeFurniture';
import {formNames} from "../../../constants/formNames";
import YouTubeKeywords from "../../../constants/youTubeKeywords";
import {getYouTubeTagCharCount} from "../../../util/getYouTubeTagCharCount";
import FieldNotification from "../../../constants/FieldNotification";

export class YoutubeFurnitureTab extends React.Component {
  static tabsRole = Tab.tabsRole;

  render() {
    return (
      <Tab {...this.props}>
        YouTube Furniture
      </Tab>
    )
  }
}

export class YoutubeFurnitureTabPanel extends React.Component {
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
      ...rest
    } = this.props;

    return (
      <TabPanel {...rest}>
        <EditSaveCancel onEdit={onEdit} onSave={onSave} onCancel={onCancel} canSave={() => true}/>
        <YoutubeFuniture
          video={video}
          editable={editing}
          updateVideo={updateVideo}
          updateErrors={updateErrors}
          updateWarnings={updateWarnings}
        />
      </TabPanel>
    )
  }
}

