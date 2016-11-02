import React, {PropTypes} from 'react';
import VideoAssetUrl from './formComponents/VideoAssetUrl';
import validate from '../../constants/videoAssetAddValidation';
import { Field, reduxForm } from 'redux-form';

const VideoAssetAdd = (props) => {

  return (
      <div className="video__sidebar video__sidebar__group">
        <Field name="url" type="text" component={VideoAssetUrl} {...props} />
      </div>
  )
};

export default reduxForm({
  form: 'VideoAssetAdd',
  validate
})(VideoAssetAdd)
