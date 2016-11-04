const validate = (values) => {
  const errors = {};
  if (!values.url) {
    errors.url = 'Required'
  }
  return errors
};

export default validate;