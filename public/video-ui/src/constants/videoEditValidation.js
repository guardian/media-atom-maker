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
  if (!values.duration) {
    errors.duration = 'Required'
  }
  return errors
};

export default validate;