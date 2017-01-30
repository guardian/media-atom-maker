import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoDurationEdit from './formComponents/VideoDuration';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import MaybeFormFieldSaveWrapper from '../FormFields/MaybeFormFieldSaveWrapper';
import VideoPosterEdit from './formComponents/VideoPoster';
import YoutubeCategorySelect from './formComponents/YoutubeCategory';
import VideoExpiryEdit from './formComponents/VideoExpiry';
import YoutubeKeywordsSelect from './formComponents/YoutubeKeywords';
import YoutubeChannelSelect from './formComponents/YoutubeChannel';
import PrivacyStatusSelect from './formComponents/PrivacyStatus';
import ContentFlags from './formComponents/ContentFlags';
import SaveButton from '../utils/SaveButton';
import validate from '../../constants/videoEditValidation';
import { Field, reduxForm } from 'redux-form';

const VideoEdit = (props) => {

    if (props.createMode) {

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
    } else {
      return (
        <div>
          <div className="form__group">
            <div className="form__group__header">Video Metadata</div>
            <FormFieldSaveWrapper
              saveVideo={props.saveVideo}
              resetVideo={props.resetVideo}
              editable={props.editable}
              saveState={props.saveState}>
              <Field
                name="title"
                type="text"
                component={VideoTitleEdit}

                video={props.video}
                updateVideo={props.updateVideo}
                editable={props.editable} />
            </FormFieldSaveWrapper>

            <FormFieldSaveWrapper
              saveVideo={props.saveVideo}
              resetVideo={props.resetVideo}
              editable={props.editable}
              saveState={props.saveState}>
              <Field
                name="category"
                type="select"
                component={VideoCategorySelect}
                video={props.video}
                updateVideo={props.updateVideo}
                editable={props.editable} />
            </FormFieldSaveWrapper>

            <FormFieldSaveWrapper {...props}>
              <Field
                name="expiry"
                type="number"
                component={VideoExpiryEdit}
                video={props.video}
                updateVideo={props.updateVideo}
                editable={props.editable} />
            </FormFieldSaveWrapper>

            <Field
              name="duration"
              type="number"
              component={VideoDurationEdit}
              video={props.video}
              updateVideo={props.updateVideo}
              editable={props.editable} />

            <Field
              name="contentFlags"
              component={ContentFlags}
              video={props.video}
              saveAndUpdateVideo={props.saveAndUpdateVideo}
              editable={props.editable} />

          </div>

          <div className="form__group">
            <div className="form__group__header">Media</div>
            <Field
              name="posterImage"
              component={VideoPosterEdit}
              video={props.video}
              editable={props.editable}
              saveAndUpdateVideo={props.saveAndUpdateVideo}/>
          </div>

          <div className="form__group">
            <div className="form__group__header">Youtube Metadata</div>

            <FormFieldSaveWrapper
              saveVideo={props.saveVideo}
              resetVideo={props.resetVideo}
              editable={props.editable}
              saveState={props.saveState}>
              <Field
                name="youtubeCategory"
                type="select"
                component={YoutubeCategorySelect}
                video={props.video}
                updateVideo={props.updateVideo}
                editable={props.editable} />
            </FormFieldSaveWrapper>

            <Field
              name="youtubeChannel"
              type="select"
              component={YoutubeChannelSelect}
              video={props.video}
              updateVideo={props.updateVideo}
              editable={props.editable} />

            <MaybeFormFieldSaveWrapper
              saveVideo={props.saveVideo}
              resetVideo={props.resetVideo}
              editable={props.editable}
              saveState={props.saveState}
              name={"privacyStatus"}
              type={"text"}
              component={PrivacyStatusSelect}
              video={props.video}
              updateVideo={props.updateVideo}
              editable={props.editable}
              disableEditing={props.disableStatusEditing}>
            </MaybeFormFieldSaveWrapper>

            <Field
              name="youtubeKeywords"
              component={YoutubeKeywordsSelect}
              video={props.video}
              saveAndUpdateVideo={props.saveAndUpdateVideo}
              editable={props.editable} />
          </div>
        </div>
      )
    }
};

export default reduxForm({
  form: 'VideoEdit',
  validate
})(VideoEdit)
