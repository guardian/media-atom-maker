import React, {PropTypes} from 'react';
import _get from 'lodash/fp/get';
import _set from 'lodash/fp/set';
import validateField from '../../util/validateField';

export class ManagedField extends React.Component {

  state = {
    fieldErrors : [],
    fieldWarnings : []
  };

  static propTypes = {
    fieldLocation: PropTypes.string.isRequired,
    children: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.arrayOf(PropTypes.element)
    ]),
    updateData: PropTypes.func,
    updateFormErrors: PropTypes.func,
    updateFormWarnings: PropTypes.func,
    data: PropTypes.object,
    fieldName: PropTypes.string,
    defaultEmptyField: PropTypes.string,
    defaultEmptyOption: PropTypes.string,
    loadingIndicator: PropTypes.string,
    isRequired: PropTypes.bool,
    isDesired: PropTypes.bool,
    editable: PropTypes.bool,
    maxLength: PropTypes.number
  };

  updateFn = (newValue) => {
    const notifications = validateField(newValue, this.props.isRequired, this.props.isDesired);

    this.setState({
      fieldErrors: notifications.errors,
      fieldWarnings: notifications.warnings
    });

    this.props.updateFormErrors(notifications.errors, this.props.name);

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
        fieldErrors: this.state.fieldErrors,
        onUpdateField: this.updateFn,
        editable: this.props.editable,
        maxLength: this.props.maxLength,
        errors: this.state.fieldErrors,
        placeholder: this.props.placeholder
      });
    });
return <div>{hydratedChildren}</div>; } }
