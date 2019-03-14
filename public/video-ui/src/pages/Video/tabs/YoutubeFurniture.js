import React from 'react';
import PropTypes from 'prop-types';
import { Tab, TabPanel } from 'react-tabs';
import EditSaveCancel from "../../../components/EditSaveCancel";
import YoutubeData from '../../../components/YoutubeData';
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

  validateYouTubeKeywords = youtubeKeywords => {
    const charLimit = YouTubeKeywords.maxCharacters;
    const numberOfChars = getYouTubeTagCharCount(youtubeKeywords);

    if (numberOfChars > charLimit) {
      return new FieldNotification(
        'required',
        `Maximum characters allowed in YouTube keywords is ${charLimit}.`,
        FieldNotification.error
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
      ...rest
    } = this.props;

    return (
      <TabPanel {...rest}>
        <EditSaveCancel onEdit={onEdit} onSave={onSave} onCancel={onCancel} canSave={() => true}/>
        <YoutubeData
          video={video}
          editable={editing}
          formName={formNames.youtubeData}
          updateVideo={updateVideo}
          updateErrors={updateErrors}
          updateWarnings={updateWarnings}
          validateYouTubeKeywords={this.validateYouTubeKeywords}
        />
      </TabPanel>
    )
  }
}
