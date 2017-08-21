import FieldNotification from '../constants/FieldNotification';
import {
  requiredForComposerWarning
} from '../constants/requiredForComposerWarning';

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

  if (
    isDesired &&
    (!fieldValue || (Array.isArray(fieldValue) && fieldValue.length === 0))
  ) {
    return new FieldNotification(
      'desired',
      requiredForComposerWarning,
      FieldNotification.warning
    );
  }

  return null;
};

export default validateField;
