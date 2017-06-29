import React from 'react';
import { PropTypes } from 'prop-types';
import _get from 'lodash/fp/get';
import _set from 'lodash/fp/set';
import validateField from '../../util/validateField';
import FieldNotification from '../../constants/FieldNotification';

export class ManagedField extends React.Component {
  static propTypes = {
    fieldLocation: PropTypes.string.isRequired,
    children: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.arrayOf(PropTypes.element)
    ]),
    updateData: PropTypes.func,
    updateFormErrors: PropTypes.func,
    updateFormWarnings: PropTypes.func,
    customValidation: PropTypes.func,
    data: PropTypes.object,
    fieldName: PropTypes.string,
    isRequired: PropTypes.bool,
    isDesired: PropTypes.bool,
    editable: PropTypes.bool,
    maxLength: PropTypes.number,
    fieldDetails: PropTypes.string,
    tagType: PropTypes.string,
    inputPlaceholder: PropTypes.string
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
  }

  checkErrorsAndWarnings(value) {
    if (this.props.updateFormErrors) {
      const notification = validateField(
        value,
        this.props.isRequired,
        this.props.isDesired,
        this.props.customValidation
      );

      if (notification && notification.type === FieldNotification.error) {
        this.props.updateFormErrors(notification, this.props.fieldLocation);
      } else {
        this.props.updateFormErrors(null, this.props.fieldLocation);
      }

      this.setState({
        fieldNotification: notification
      });
    }
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
    if (!this.props.editable && this.props.placeholder && !value) {
      return this.props.placeholder;
    }

    if (!value) {
      return '';
    }

    return value;
  }

  hasError(props) {
    return (
      props.touched &&
      props.notification &&
      props.notification.type === FieldNotification.error
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
    const hydratedChildren = React.Children.map(this.props.children, child => {
      return React.cloneElement(child, {
        fieldName: this.props.name,
        fieldValue: this.getFieldValue(
          _get(this.props.fieldLocation, this.props.data)
        ),
        onUpdateField: this.updateFn,
        editable: this.props.editable,
        maxLength: this.props.maxLength,
        notification: this.state.fieldNotification,
        placeholder: this.props.placeholder,
        touched: this.state.touched,
        fieldDetails: this.props.fieldDetails,
        hasError: this.hasError,
        hasWarning: this.hasWarning,
        displayPlaceholder: this.displayPlaceholder,
        derivedFrom: this.props.derivedFrom,
        maxCharLength: this.props.maxCharLength,
        tagType: this.props.tagType,
        inputPlaceholder: this.props.inputPlaceholder
      });
    });
    return <div className="form-element">{hydratedChildren}</div>;
  }
}
