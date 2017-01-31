import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoDurationEdit from './formComponents/VideoDuration';
import VideoPosterEdit from './formComponents/VideoPoster';
import YoutubeCategorySelect from './formComponents/YoutubeCategory';
import VideoExpiryEdit from './formComponents/VideoExpiry';
import YoutubeKeywordsSelect from './formComponents/YoutubeKeywords';
import YoutubeChannelSelect from './formComponents/YoutubeChannel';
import PrivacyStatusSelect from './formComponents/PrivacyStatus';
import ContentFlags from './formComponents/ContentFlags';
import SaveButton from '../utils/SaveButton';
import validate from '../../constants/videoEditValidation';
import YoutubeMetaData from '../YoutubeMetaData/YoutubeMetaData';
import VideoMetaData from '../VideoMetaData/VideoMetaData';
import { Field, reduxForm } from 'redux-form';

const VideoEdit = (props) => {

  return (
    <div>
      <Field
      name="title"
      type="text"
      component={VideoTitleEdit}
      video={props.video}
      updateVideo={props.updateVideo}
      editable={props.editable} />

      <Field
        name="category"
        type="select"
        component={VideoCategorySelect}
        video={props.video}
        updateVideo={props.updateVideo}
        editable={props.editable} />

      <Field
        name="posterImage"
        component={VideoPosterEdit}
        video={props.video}
        editable={props.editable}
        saveAndUpdateVideo={props.saveAndUpdateVideo} />

      <Field
        name="youtubeCategory"
        type="select"
        component={YoutubeCategorySelect}
        video={props.video}
        updateVideo={props.updateVideo}
        editable={props.editable} />

      <Field
        name="videoExpiry"
        name="expiry"
        type="select"
        component={VideoExpiryEdit}
        video={props.video}
        updateVideo={props.updateVideo}
        editable={props.editable} />

      <Field
        name="youtube-channel"
        type="select"
        component={YoutubeChannelSelect}
        video={props.video}
        updateVideo={props.updateVideo}
        editable={props.editable} />

      <Field
        name="privacyStatus"
        type="text"
        component={PrivacyStatusSelect}
        video={props.video}
        updateVideo={props.updateVideo}
        editable={props.editable} />

      <SaveButton saveState={props.saveState.saving} onSaveClick={props.saveVideo} onResetClick={props.resetVideo} />
    </div>
  )
};

export default reduxForm({
  form: 'VideoEdit',
  validate
})(VideoEdit)
