import React, {PropTypes} from 'react';
import _get from 'lodash/fp/get';
import _set from 'lodash/fp/set';
import validateField from '../../util/validateField';

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
    fieldDetails: PropTypes.string
  };

  state = {
    fieldError : [],
    fieldWarning : [],
    touched : false
  };

  componentDidMount() {
    const value = this.props.data ? this.props.data[this.props.fieldLocation] : null;
    this.checkErrorsAndWarnings(value);
  }

  checkErrorsAndWarnings(value) {

    if (this.props.updateFormErrors) {
      const notifications = validateField(value, this.props.isRequired, this.props.isDesired, this.props.customValidation);
      this.setState({
        fieldError: notifications.error,
        fieldWarning: notifications.warning
      });

      this.props.updateFormErrors(notifications.error, this.props.fieldLocation);
    }

  }

  updateFn = (newValue) => {

    this.setState({
      touched: true
    });

    this.checkErrorsAndWarnings(newValue);

    if (newValue !== '') {
      this.props.updateData(_set(this.props.fieldLocation, newValue, this.props.data));
    } else {
      this.props.updateData(_set(this.props.fieldLocation, null, this.props.data));
    }
  }

  getFieldValue(value) {
    if (!this.props.editable && this.props.placeholder && !value) {
      return this.props.placeholder;
    }

    if (!value) {
      return '';
    }

    return value;

  }


  render () {

    const hydratedChildren = React.Children.map(this.props.children, (child) => {
      return React.cloneElement(child, {
        fieldName: this.props.name,
        fieldValue: this.getFieldValue(_get(this.props.fieldLocation, this.props.data)),
        onUpdateField: this.updateFn,
        editable: this.props.editable,
        maxLength: this.props.maxLength,
        error: this.state.fieldError,
        warning: this.state.fieldWarning,
        placeholder: this.props.placeholder,
        touched: this.state.touched,
        fieldDetails: this.props.fieldDetails
      });
    });
    return <div>{hydratedChildren}</div>;
  }
}
