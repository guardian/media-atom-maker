import React from 'react';
import TextInput from '../FormFields/TextInput';
import VideoTitleEdit from '../VideoEdit/formComponents/VideoTitle';
import { Field, reduxForm } from 'redux-form';
import validate from '../../constants/videoEditValidation';
import FormFieldSaveWrapper from '../FormFields/FormFieldSaveWrapper';
import MaybeFormFieldSaveWrapper from '../FormFields/MaybeFormFieldSaveWrapper';
import YoutubeKeywordsSelect from '../VideoEdit/formComponents/YoutubeKeywords';
import YoutubeChannelSelect from '../VideoEdit/formComponents/YoutubeChannel';
import PrivacyStatusSelect from '../VideoEdit/formComponents/PrivacyStatus';
import ContentFlags from '../VideoEdit/formComponents/ContentFlags';


const YoutubeMetaData = (props) => {

    return (
      <div className="form__group">
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
          updateVideo={props.updateVideo}
          editable={props.editable} />
      </div>
    );
  }

export default reduxForm({
  form: 'YoutubeMetaData',
  validate
})(YoutubeMetaData)
