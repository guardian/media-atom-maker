import FieldNotification from '../constants/FieldNotification';

const validateField = (
  fieldValue,
  isRequired: false,
  isDesired: false,
  customValidation: null
) => {
  if (customValidation) {
    return customValidation(fieldValue);
  }

  if (isRequired && !fieldValue) {
    return new FieldNotification(
      'required',
      'This field is required',
      FieldNotification.error
    );
  }

  if (isDesired && !fieldValue) {
    return new FieldNotification(
      'desired',
      'This field is recommended',
      FieldNotification.warning
    );
  }

  return null;
};

export default validateField;
