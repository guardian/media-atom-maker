const videoEditValidation = (values) => {
  const errors = {};
  if (!values.title) {
    errors.username = 'Required'
  }
  if (!values.category) {
    errors.category = 'Required'
  }
  if (!values.poster) {
    errors.poster = 'Required'
  }
  return errors
};

export default videoEditValidation;