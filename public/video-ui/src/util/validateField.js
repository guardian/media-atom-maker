import FieldNotification from '../constants/FieldNotification';

const validateField = (fieldValue, isRequired: false, isDesired: false, customValidation) => {
  const errors = [];
  const warnings = [];

  if (isRequired && !fieldValue) {
    const error = new FieldNotification('required', 'This field is required');
    errors.push(error);
  }

  else if (isDesired && !fieldValue) {
    const warning = new FieldNotification('desired', 'It is recommended that you fill in this field');
    warnings.push(warning);
  }

  return {
    errors: errors,
    warnings: warnings
  };
};

export default validateField;
