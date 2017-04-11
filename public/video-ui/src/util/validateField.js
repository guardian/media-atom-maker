import FieldNotification from '../constants/FieldNotification';

const validateField = (fieldValue, isRequired: false, isDesired: false, customValidation: null) => {

  function withWarning(warning) {
    return {
      error: null,
      warning: warning
    };
  }

  function withError(error) {
    return {
      error: error,
      warning: null
    };
  }

  if (customValidation) {
    const customResults = customValidation(fieldValue);
    if (customResults.error) {
      return withError(customResults.error);
    }

    if (customResults.warning) {
      return withWarning(customResults.warning);
    }
  }

  if (isRequired && !fieldValue) {
    return withError(new FieldNotification('required', 'This field is required'));
  }

  if (isDesired && !fieldValue) {
    return withWarning(new FieldNotification('desired', 'This field is recommended'));
  }

  return {
    error: null,
    warning: null
  };

};

export default validateField;
