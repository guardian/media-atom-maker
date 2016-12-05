import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoDurationEdit from './formComponents/VideoDuration';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import VideoPosterEdit from './formComponents/VideoPoster';
import YoutubeCategorySelect from './formComponents/YoutubeCategory';
import YoutubeKeywordsSelect from './formComponents/YoutubeKeywords';
import YoutubeChannelSelect from './formComponents/YoutubeChannel';
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

          <Field
            name="youtube-channel"
            type="select"
            component={YoutubeChannelSelect}
            {...props} />

          <SaveButton saveState={props.saveState} onSaveClick={props.saveVideo} onResetClick={props.resetVideo} />
        </div>
      )
    } else {
      return (
        <div>
          <div className="form__group">
            <div className="form__group__header">Video Metadata</div>
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
          </div>

          <div className="form__group">
            <div className="form__group__header">Media</div>
            <Field name="posterImage" component={VideoPosterEdit} {...props} />
          </div>

          <div className="form__group">
            <div className="form__group__header">Youtube Metadata</div>

            <FormFieldSaveWrapper {...props}>
              <Field
                name="youtube-channel"
                type="select"
                component={YoutubeChannelSelect}
                {...props} />
            </FormFieldSaveWrapper>

            <FormFieldSaveWrapper {...props}>
              <Field
                name="youtube-category"
                type="select"
                component={YoutubeCategorySelect}
                {...props} />
            </FormFieldSaveWrapper>

            <Field name="youtube-keywords" component={YoutubeKeywordsSelect} {...props} />
          </div>

        {props.showSelect ? <button className="btn" onClick={props.onSelectVideo}>Select this Atom</button> : false}
        </div>
      )
    }
};

export default reduxForm({
  form: 'VideoEdit',
  validate
})(VideoEdit)
