import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoDurationEdit from './formComponents/VideoDuration';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import VideoPosterEdit from './formComponents/VideoPoster';
import YoutubeCategorySelect from './formComponents/YoutubeCategory';
import YoutubeChannelSelect from './formComponents/YoutubeChannel';
import PrivacyStatusSelect from './formComponents/PrivacyStatus';
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
          {...props} />

          <Field
            name="category"
            type="select"
            component={VideoCategorySelect}
            {...props} />

          <Field
            name="duration"
            type="number"
            component={VideoDurationEdit}
            {...props} />

          <Field name="posterImage" component={VideoPosterEdit} {...props} />

          <Field
            name="youtube-category"
            type="select"
            component={YoutubeCategorySelect}
            {...props} />
          <SaveButton saveState={props.saveState} onSaveClick={props.saveVideo} onResetClick={props.resetVideo} />
        </div>
      )
    } else {
      return (
        <div>

          <FormFieldSaveWrapper {...props}>
            <Field
              name="title"
              type="text"
              component={VideoTitleEdit}
              {...props} />
          </FormFieldSaveWrapper>

          <FormFieldSaveWrapper {...props}>
            <Field
              name="category"
              type="select"
              component={VideoCategorySelect}
              {...props} />
          </FormFieldSaveWrapper>

          <FormFieldSaveWrapper {...props}>
            <Field
              name="duration"
              type="number"
              component={VideoDurationEdit}
              {...props} />
          </FormFieldSaveWrapper>

          <Field name="posterImage" component={VideoPosterEdit} {...props} />

          <FormFieldSaveWrapper {...props}>
            <Field
              name="youtube-category"
              type="select"
              component={YoutubeCategorySelect}
              {...props} />
          </FormFieldSaveWrapper>

          <FormFieldSaveWrapper {...props}>
            <Field name="youtube-channel"
                   type="text"
                   component={YoutubeChannelSelect}
                   {...props} />
          </FormFieldSaveWrapper>

          <FormFieldSaveWrapper {...props}>
            <Field name="privacy-status"
                   type="text"
                   component={PrivacyStatusSelect}
                   {...props} />
          </FormFieldSaveWrapper>

          {props.showSelect ? <button className="btn" onClick={props.onSelectVideo}>Select this Atom</button> : false}
        </div>
      )
    }
};

export default reduxForm({
  form: 'VideoEdit',
  validate
})(VideoEdit)
