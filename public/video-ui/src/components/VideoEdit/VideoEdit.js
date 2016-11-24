import React from 'react';
import VideoTitleEdit from './formComponents/VideoTitle';
import VideoCategorySelect from './formComponents/VideoCategory';
import VideoDurationEdit from './formComponents/VideoDuration';
import VideoPosterEdit from './formComponents/VideoPoster';
import YoutubeCategorySelect from './formComponents/YoutubeCategory';
import YoutubeKeywordsSelect from './formComponents/YoutubeKeywords';
import YoutubeChannelSelect from './formComponents/YoutubeChannel';
import validate from '../../constants/videoEditValidation';
import { Field, reduxForm } from 'redux-form';

const VideoEdit = (props) => {
    return (
        <div>
          <Field name="title" type="text" component={VideoTitleEdit} {...props} />
          <Field name="category" type="text" component={VideoCategorySelect} {...props} />
          <Field name="duration" type="number" component={VideoDurationEdit} {...props} />
          <Field name="posterImage" component={VideoPosterEdit} {...props} />
          <Field name="youtube-category" type="text" component={YoutubeCategorySelect} {...props} />
          <Field name="youtube-channel" type="text" component={YoutubeChannelSelect} {...props} />
          <Field name="youtube-keywords" component={YoutubeKeywordsSelect} {...props} />
        </div>
    )
};

export default reduxForm({
  form: 'VideoEdit',
  validate
})(VideoEdit)
