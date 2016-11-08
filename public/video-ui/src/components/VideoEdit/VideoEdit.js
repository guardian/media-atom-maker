import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoPosterImageEdit from './formComponents/VideoPosterImage';
import VideoDurationEdit from './formComponents/VideoDuration';
import VideoDescriptionEdit from './formComponents/VideoDescription';
import validate from '../../constants/videoEditValidation';
import warn from '../../constants/videoEditWarnings';
import { Field, reduxForm } from 'redux-form';

const VideoEdit = (props) => {
    return (
        <div>
          <Field name="title" type="text" component={VideoTitleEdit} {...props} />
          <Field name="category" type="text" component={VideoCategorySelect} {...props} />
          <Field name="posterUrl" type="text" component={VideoPosterImageEdit} {...props} />
          <Field name="duration" type="number" component={VideoDurationEdit} {...props} />
          <Field name="description" type="number" component={VideoDescriptionEdit} {...props} />
        </div>
    )
};

export default reduxForm({
  form: 'VideoEdit',
  validate,
  warn
})(VideoEdit)
