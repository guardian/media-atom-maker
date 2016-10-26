const validate = (values) => {
  const errors = {};
  if (!values.title) {
    errors.title = 'Required'
  }
  if (!values.category) {
    errors.category = 'Required'
  }
  if (!values.posterUrl) {
    errors.posterUrl = 'Required'
  }
  return errors
};

export default validate;