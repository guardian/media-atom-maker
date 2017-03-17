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
    //custom validation ???

  };

  updateFn = (event) => {
    const newValue = event.target.value;
    const notifications = validateField(newValue, this.props.isRequired, this.props.isDesired);

    this.setState({
      fieldErrors: notifications.errors,
      fieldWarnings: notifications.warnings
    });

    this.props.updateFormErrors(notifications.errors, this.props.name);
    //custom validation here?
    //select boxes: check if the value is the same as default empty option
    //
    if (this.props.defaultEmptyOption && this.props.defaultEmptyOption === newValue) {
      return;
    }

    this.props.updateData(_set(this.props.fieldLocation, newValue, this.props.data));
  }


  render () {

    const hydratedChildren = React.Children.map(this.props.children, (child) => {
      return React.cloneElement(child, {
        fieldName: this.props.name,
        fieldValue: _get(this.props.fieldLocation, this.props.data),
        fieldErrors: this.state.fieldErrors,
        onUpdateField: this.updateFn,
        editable: this.props.editable,
        maxLength: this.props.maxLength,
        errors: this.state.fieldErrors
      });
    });

    return <div>{hydratedChildren}</div>;
  }
}

