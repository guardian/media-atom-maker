import React from 'react';
import VideoTitleEdit from '../VideoEdit/formComponents/VideoTitle';
import VideoDescriptionEdit from '../VideoEdit/formComponents/VideoDescription';
import VideoDurationEdit from '../VideoEdit/formComponents/VideoDuration';
import ContentFlags from '../VideoEdit/formComponents/ContentFlags';
import VideoExpiryEdit from '../VideoEdit/formComponents/VideoExpiry';
import VideoCategorySelect from '../VideoEdit/formComponents/VideoCategory';
import { Field, reduxForm } from 'redux-form';
import {validate} from '../../constants/videoEditValidation';

const VideoMetaData = (props) => {

    return (
        <div className="form__group">
          <Field
            name="title"
            type="text"
            component={VideoTitleEdit}
            video={props.video}
            updateVideo={props.updateVideo}
            editable={props.editable} />

          <Field
            name="description"
            type="text"
            component={VideoDescriptionEdit}
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
            name="expiry"
            type="number"
            component={VideoExpiryEdit}
            video={props.video}
            updateVideo={props.updateVideo}
            editable={props.editable} />

          <Field
            name="duration"
            type="number"
            component={VideoDurationEdit}
            video={props.video}
            editable={false} />

          <Field
            name="contentFlags"
            component={ContentFlags}
            video={props.video}
            updateVideo={props.updateVideo}
            editable={props.editable} />
        </div>
    );
  };

export default reduxForm({
  form: 'VideoMetaData',
  validate
})(VideoMetaData);
