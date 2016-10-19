import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoPosterImageEdit from './formComponents/VideoPosterImage';
import videoEditValidation from '../../constants/videoEditValidation';
import { Field, reduxForm } from 'redux-form';


const VideoEdit = (props) => {
  return (
      <div>
        <Field name="title" type="text" component={VideoTitleEdit} {...props} />
        <Field name="category" type="text" component={VideoCategorySelect} {...props} />
        <Field name="poster" type="text" component={VideoPosterImageEdit} {...props} />
      </div>
  )
}

export default reduxForm({
  form: 'syncValidation',
  videoEditValidation
})(VideoEdit)
