export const validate = (values) => {
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

export const fieldLengths = {
  title: 100 // https://developers.google.com/youtube/v3/docs/videos#snippet.title
};
