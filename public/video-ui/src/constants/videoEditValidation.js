const validate = (values) => {
  const errors = {};
  if (!values) {
    return {};
  }
  // Required fields
  if (!values.title) {
    errors.title = 'Required'
  }
  if (!values.category) {
    errors.category = 'Required'
  }
  if (!values.posterImage) {
    errors.posterImage = 'Required'
  }
  if (!values.duration) {
    errors.duration = 'Required'
  }
  if(!values.youtubeCategory) {
    errors.youtubeCategory = 'Required'
  }
  if(!values.youtubeChannel) {
    errors.youtubeChannel = 'Required'
  }
  if(!values.privacyStatus) {
    errors.privacyStatus = 'Required'
  }
  // Data errors
  if (values.duration <= 0) {
    errors.duration = 'The duration must be longer than 0 seconds'
  }
  return errors
};

export default validate;
