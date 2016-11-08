import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoPosterImageEdit from './formComponents/VideoPosterImage';
import VideoDurationEdit from './formComponents/VideoDuration';
import VideoDescriptionEdit from './formComponents/VideoDescription';
import VideoChannelIdEdit from './formComponents/VideoChannelId';
import VideoCategoryIdEdit from './formComponents/VideoCategoryId';
import VideoCommentsEdit from './formComponents/VideoComments';
import VideoLicenseEdit from './formComponents/VideoLicense';
import VideoTagsEdit from './formComponents/VideoTags';
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
          <Field name="description" type="text" component={VideoDescriptionEdit} {...props} />
          <Field name="channelId" type="text" component={VideoChannelIdEdit} {...props} />
          <Field name="categoryId" type="text" component={VideoCategoryIdEdit} {...props} />
          <Field name="commentsEnabled" type="checkbox" component={VideoCommentsEdit} {...props} />
          <Field name="license" type="text" component={VideoLicenseEdit} {...props} />
          <Field name="tags" type="text" component={VideoTagsEdit} {...props} />
        </div>
    )
};

export default reduxForm({
  form: 'VideoEdit',
  validate,
  warn
})(VideoEdit)
