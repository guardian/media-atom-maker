import React from 'react';
import { Field, reduxForm } from 'redux-form';
import VideoPosterEdit from '../VideoEdit/formComponents/VideoPoster';
import validate from '../../constants/posterEditValidation';

const PosterEdit = (props) => {

  return (
    <Field
      name="posterImage"
      component={VideoPosterEdit}
      video={props.video || {}}
      updateVideo={props.updateVideo}
      editable={props.editable}
      editMode={true}
    />
  );
};

export default reduxForm({
    form: 'PosterEdit',
    validate
})(PosterEdit);
