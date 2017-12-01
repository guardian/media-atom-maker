import FieldNotification from '../constants/FieldNotification';
import RequiredForComposer from '../constants/requiredForComposer';

const validateField = (
  fieldValue,
  isRequired: false,
  isDesired: false,
  customValidation: null,
  composerValidation: false
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
  if (
    isDesired && fieldValueMissing()
  ) {
    return new FieldNotification(
      'desired',
      composerValidation ? RequiredForComposer.warning : 'This field is desired',
      FieldNotification.warning
    );
  }

  return null;
};

export default validateField;
