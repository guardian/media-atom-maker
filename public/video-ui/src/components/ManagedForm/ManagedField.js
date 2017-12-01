import React from 'react';
import { PropTypes } from 'prop-types';
import _get from 'lodash/fp/get';
import _set from 'lodash/fp/set';
import validateField from '../../util/validateField';
import {getTextFromHtml} from '../../util/getTextFromHtml';
import FieldNotification from '../../constants/FieldNotification';
import {fieldsWithHtml} from '../../constants/fieldsWithHtml';
import RequiredForComposer from '../../constants/requiredForComposer';

export class ManagedField extends React.Component {
  static propTypes = {
    fieldLocation: PropTypes.string.isRequired,
    children: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.arrayOf(PropTypes.element)
    ]),
    updateData: PropTypes.func,
    updateFormErrors: PropTypes.func,
    updateWarnings: PropTypes.func,
    customValidation: PropTypes.func,
    data: PropTypes.object,
    fieldName: PropTypes.string,
    isRequired: PropTypes.bool,
    isDesired: PropTypes.bool,
    disabled: PropTypes.bool,
    editable: PropTypes.bool,
    maxLength: PropTypes.number,
    fieldDetails: PropTypes.string,
    tagType: PropTypes.string,
    inputPlaceholder: PropTypes.string,
    tooltip: PropTypes.string,
    fieldLocation: PropTypes.string,
    updateSideEffects: PropTypes.func
  };

  state = {
    fieldNotification: null,
    touched: false
  };

  componentDidMount() {
    const value = this.props.data
      ? this.props.data[this.props.fieldLocation]
      : null;
    this.checkErrorsAndWarnings(value);

    this.placeholder = 'No ' + this.props.name.split('(')[0];
  }

  checkErrorsAndWarnings(value) {

    if (value && fieldsWithHtml.includes(this.props.fieldLocation)) {
      value = getTextFromHtml(value);
    }

    const composerValidation = RequiredForComposer.fields.includes(this.props.fieldLocation);

    const notification = validateField(
      value,
      this.props.isRequired,
      this.props.isDesired,
      this.props.customValidation,
      composerValidation
    );

    if (this.props.updateFormErrors) {
      if (notification && notification.type === FieldNotification.error) {
        this.props.updateFormErrors(notification, this.props.fieldLocation);
      } else {
        this.props.updateFormErrors(null, this.props.fieldLocation);
      }
    }

    if (this.props.updateWarnings) {

      if (notification && notification.type === FieldNotification.warning) {
        this.props.updateWarnings(true, this.props.fieldLocation);

      } else {
        this.props.updateWarnings(false, this.props.fieldLocation);

      }
    }

    this.setState({
      fieldNotification: notification
    });
  }

  updateFn = newValue => {
    this.setState({
      touched: true
    });

    this.checkErrorsAndWarnings(newValue);

    if (newValue !== '') {
      this.props.updateData(
        _set(this.props.fieldLocation, newValue, this.props.data)
      );
    } else {
      this.props.updateData(
        _set(this.props.fieldLocation, null, this.props.data)
      );
    }
  };

  getFieldValue(value) {
    if (!this.props.editable && !value) {
      return this.placeholder;
    }

    if (!value) {
      return '';
    }

    return value;
  }

  hasError(props) {
    return (
      props.notification && props.notification.type === FieldNotification.error
    );
  }

  hasWarning(props) {
    return (
      props.notification &&
      props.notification.type === FieldNotification.warning
    );
  }

  displayPlaceholder(placeholder, fieldValue) {
    return placeholder && placeholder === fieldValue;
  }

  render() {
    let editable = this.props.editable;
    let className = 'form-element';

    if (this.props.disabled) {
      editable = false;
      className += ' form-element--hidden';
    }

    const hydratedChildren = React.Children.map(this.props.children, child => {
      return React.cloneElement(child, {
        fieldName: this.props.name,
        fieldValue: this.getFieldValue(
          _get(this.props.fieldLocation, this.props.data)
        ),
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
        derivedFrom: this.props.derivedFrom,
        maxCharLength: this.props.maxCharLength,
        tagType: this.props.tagType,
        inputPlaceholder: this.props.inputPlaceholder,
        tooltip: this.props.tooltip,
        fieldLocation: this.props.fieldLocation,
        updateSideEffects: this.props.updateSideEffects
      });
    });
    return <div className={className}>{hydratedChildren}</div>;
  }
}
