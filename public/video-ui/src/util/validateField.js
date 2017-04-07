import FieldNotification from '../constants/FieldNotification';

const validateField = (fieldValue, isRequired: false, isDesired: false) => {
  const errors = [];
  const warnings = [];

  if (isRequired && !fieldValue) {
    const error = new FieldNotification('required', 'This field is required');
    errors.push(error);
  }

  else if (isDesired && !fieldValue) {
    const warning = new FieldNotification('desired', 'This field is recommended');
    warnings.push(warning);
  }

  return {
    errors: errors,
    warnings: warnings
  };
};

export default validateField;
