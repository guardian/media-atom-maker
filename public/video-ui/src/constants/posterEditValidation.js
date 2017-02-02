const validate = (values) => {
  const errors = {};
  if (!values) {
    return {};
  }
  // Required fields
  if (!values.posterImage) {
    errors.posterImage = 'Required';
  }
  return errors;
};

export default validate;
