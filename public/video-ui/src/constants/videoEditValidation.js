const validate = (values) => {
  const errors = {};
  if (values) {
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
  }
  return errors
};

export default validate;
