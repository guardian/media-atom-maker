import React from 'react';
import _get from 'lodash/fp/get';
import _set from 'lodash/fp/set';
import validateField from '../../util/validateField';
import { getTextFromHtml } from '../../util/getTextFromHtml';
import FieldNotification from '../../constants/FieldNotification';
import { fieldsWithHtml } from '../../constants/fieldsWithHtml';
import RequiredForComposer from '../../constants/requiredForComposer';
import RequiredForDefaultVideo from '../../constants/requiredForDefaultVideo';

type Props = {
  fieldLocation: string;
  updateData?: (...args: any[]) => any;
  updateFormErrors?: (...args: any[]) => any;
  updateWarnings?: (...args: any[]) => any;
  customValidation?: (...args: any[]) => any;
  data?: any;
  fieldName?: string;
  isRequired?: boolean;
  isDesired?: boolean;
  disabled?: boolean;
  editable?: boolean;
  maxLength?: number;
  fieldDetails?: string;
  tagType?: string;
  inputPlaceholder?: string;
  tooltip?: string;
  updateSideEffects?: (...args: any[]) => any;
};

type State = {
  fieldNotification: null | FieldNotification;
  touched: boolean;
};

export class ManagedField extends React.Component<Props, State> {
  placeholder: any;

  state = {
    // @ts-expect-error TS(7018): Object literal's property 'fieldNotification' impl... Remove this comment to see the full error message
    fieldNotification: null,
    touched: false
  };

  componentDidMount() {
    const value = this.props.data
      ? this.props.data[this.props.fieldLocation]
      : null;
    this.checkErrorsAndWarnings(value);

    this.placeholder = 'No ' + (this.props as any).name.split('(')[0];
  }

  checkErrorsAndWarnings(value: string) {
    if (value && fieldsWithHtml.includes(this.props.fieldLocation)) {
      value = getTextFromHtml(value);
    }

    const composerValidation = RequiredForComposer.fields.includes(
      this.props.fieldLocation
    );
    const defaultVideoValidation = RequiredForDefaultVideo.fields.includes(
      this.props.fieldLocation
    );

    const notification = validateField(
      value,
      this.props.isRequired,
      this.props.isDesired,
      this.props.customValidation,
      composerValidation,
      defaultVideoValidation,
      this.props.maxLength
    );

    if (this.props.updateFormErrors) {
      if (notification && notification.type === FieldNotification.error) {
        this.props.updateFormErrors(notification, this.props.fieldLocation);
      } else {
        this.props.updateFormErrors(null, this.props.fieldLocation);
      }
    }

    if (this.props.updateWarnings) {
      // despite its general function name "updateWarnings", this function "updateWarnings" updates
      // the slice "formFieldWarnings" in the store, which is used in the "Header" component to decide
      // whether a composer page can be created. It assumes that all warnings in the slice come from
      // composer related fields.
      if (
        notification &&
        notification.type === FieldNotification.warning &&
        composerValidation
      ) {
        this.props.updateWarnings(true, this.props.fieldLocation);
      } else {
        this.props.updateWarnings(false, this.props.fieldLocation);
      }
    }

    this.setState({
      fieldNotification: notification
    });
  }

  updateFn = (newValue: string) => {
    this.setState({
      touched: true
    });

    this.checkErrorsAndWarnings(newValue);

    return this.props
      .updateData(
        _set(
          this.props.fieldLocation,
          newValue === '' ? null : newValue,
          this.props.data
        )
      )
      .then(() => {
        if (this.props.updateSideEffects) {
          return this.props.updateSideEffects(this.props.data);
        }
      });
  };

  getFieldValue(value: any) {
    if (!this.props.editable && !value) {
      return this.placeholder;
    }

    if (!value) {
      return '';
    }

    return value;
  }

  hasError(props: { notification: { type: string } }) {
    return (
      props.notification && props.notification.type === FieldNotification.error
    );
  }

  hasWarning(props: { notification: { type: string } }) {
    return (
      props.notification &&
      props.notification.type === FieldNotification.warning
    );
  }

  displayPlaceholder(placeholder: any, fieldValue: any) {
    return placeholder && placeholder === fieldValue;
  }

  render() {
    let editable = this.props.editable;
    let className = 'form-element';

    if (this.props.disabled) {
      editable = false;
      className += ' form-element--hidden';
    }

    const hydratedChildren = React.Children.map(
      (this.props as any).children,
      child => {
        return React.cloneElement(child, {
          fieldName: (this.props as any).name,
          fieldValue: this.getFieldValue(
            _get(this.props.fieldLocation, this.props.data)
          ),
          rawFieldValue: _get(this.props.fieldLocation, this.props.data),
          onUpdateField: this.updateFn,
          editable,
          maxLength: this.props.maxLength,
          notification: this.state.fieldNotification,
          placeholder: this.placeholder,
          touched: this.state.touched,
          fieldDetails: this.props.fieldDetails,
          hasError: this.hasError,
          hasWarning: this.hasWarning,
          displayPlaceholder: this.displayPlaceholder,
          derivedFrom: (this.props as any).derivedFrom,
          // @ts-expect-error TS(2551): Property 'maxWordLength' does not exist on type 'R... Remove this comment to see the full error message
          maxWordLength: this.props.maxWordLength,
          tagType: this.props.tagType,
          inputPlaceholder: this.props.inputPlaceholder,
          tooltip: this.props.tooltip,
          fieldLocation: this.props.fieldLocation,
          updateSideEffects: this.props.updateSideEffects
        });
      }
    );
    return <div className={className}>{hydratedChildren}</div>;
  }
}
