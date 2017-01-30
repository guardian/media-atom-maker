import React from 'react';
import TextInput from '../FormFields/TextInput';
import VideoTitleEdit from '../VideoEdit/formComponents/VideoTitle';
import VideoDurationEdit from '../VideoEdit/formComponents/VideoDuration';
import ContentFlags from '../VideoEdit/formComponents/ContentFlags';
import VideoExpiryEdit from '../VideoEdit/formComponents/VideoExpiry';
import VideoCategorySelect from '../VideoEdit/formComponents/VideoCategory';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import MaybeFormFieldSaveWrapper from '../FormFields/MaybeFormFieldSaveWrapper';
import { Field, reduxForm } from 'redux-form';
import validate from '../../constants/videoEditValidation';


const VideoMetaData = (props) => {

    return (
        <div className="form__group">
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
            updateVideo={props.updateVideo}
            editable={props.editable} />
        </div>
    );
  }

export default reduxForm({
  form: 'VideoMetaData',
  validate
})(VideoMetaData)
