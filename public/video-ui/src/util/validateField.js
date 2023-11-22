import FieldNotification from '../constants/FieldNotification';
import RequiredForComposer from '../constants/requiredForComposer';

const validateField = (
  fieldValue,
  isRequired = false,
  isDesired = false,
  customValidation = null,
  composerValidation = false,
  maxLength
) => {
  if (customValidation) {
    return customValidation(fieldValue);
  }

  function fieldValueMissing() {
    if (!fieldValue) {
      return true;
    }

    if (Array.isArray(fieldValue) && fieldValue.length === 0) {
      return true;
    }

    return false;
  }

  if (isRequired && fieldValueMissing()) {
    return new FieldNotification(
      'required',
      composerValidation ? RequiredForComposer.error : 'This field is required',
      FieldNotification.error
    );
  }
  if (isDesired && fieldValueMissing()) {
    return new FieldNotification(
      'desired',
      composerValidation
        ? RequiredForComposer.warning
        : 'This field is desired',
      FieldNotification.warning
    );
  }

  function fieldValueTooLong() {
    if (fieldValue && fieldValue.length === maxLength) {
      return true;
    }

    return false;
  }

  if (maxLength && fieldValueTooLong()) {
    return new FieldNotification(
      'too long',
      'You have reached the maximum character length',
      FieldNotification.error
    );
  }

  return null;
};

export default validateField;
