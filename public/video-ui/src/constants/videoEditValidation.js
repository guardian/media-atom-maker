const validate = (values) => {
 const errors = {};
  if (!values) {
    return {};
  }
  // Required fields
  if (!values.title) {
    errors.title = 'Required';
  }
  if (!values.category) {
    errors.category = 'Required';
  }

  if (!values.posterImage) {
    errors.posterImage = 'Required';
  }

  if (!values.duration) {
    errors.duration = 'Required';
  }
  if(!values.youtubeCategory) {
    errors.youtubeCategory = 'Required';
  }
  if(!values.youtubeChannel) {
    errors.youtubeChannel = 'Required';
  }
  if(!values.privacyStatus) {
    errors.privacyStatus = 'Required';
  }
  return errors;
};

export default validate;
