import FieldNotification from '../constants/FieldNotification';
import RequiredForComposer from '../constants/requiredForComposer';
import RequiredForDefaultVideo from '../constants/requiredForDefaultVideo';

const validateField = <FieldValueType>(
  fieldValue: FieldValueType,
  isRequired: boolean = false,
  isDesired: boolean = false,
  customValidation: ((fieldValue: FieldValueType) => FieldNotification | null) | null = null,
  composerValidation: boolean = false,
  defaultVideoValidation: boolean = false,
  maxLength?: number
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

  const getMessage = () => {
    if (composerValidation)
      return {error: RequiredForComposer.error, warning: RequiredForComposer.warning};
    if (defaultVideoValidation)
      return {error: RequiredForDefaultVideo.error, warning: RequiredForDefaultVideo.warning};
    return {error: 'This field is required', warning: 'This field is desired'};
  };

  if (isRequired && fieldValueMissing()) {
    return new FieldNotification(
      'required',
      getMessage().error,
      FieldNotification.error
    );
  }
  if (isDesired && fieldValueMissing()) {
    return new FieldNotification(
      'desired',
      getMessage().warning,
      FieldNotification.warning
    );
  }

  function fieldValueTooLong() {
    if (fieldValue && (fieldValue as any).length === maxLength) {
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
