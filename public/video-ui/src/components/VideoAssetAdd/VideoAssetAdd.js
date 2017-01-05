import React, {PropTypes} from 'react';
import VideoAssetUrl from './formComponents/VideoAssetUrl';
import { Field, reduxForm } from 'redux-form';

const VideoAssetAdd = (props) => {

  return (
      <div>
        <Field name="url" type="text" component={VideoAssetUrl} {...props} />
      </div>
  )
};

export default reduxForm({
  form: 'VideoAssetAdd'
})(VideoAssetAdd)
