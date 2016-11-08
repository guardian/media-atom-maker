const warn = (values) => {
  const warnings = {};
  if (!values.description) {
    warnings.description= 'Description field is empty'
  }

  return warnings;
};

export default warn;