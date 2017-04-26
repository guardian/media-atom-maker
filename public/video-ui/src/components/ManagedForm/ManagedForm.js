import React from 'react';
import {PropTypes} from 'prop-types';

export class ManagedForm extends React.Component {

  static propTypes =  {
    data: PropTypes.object,
    updateData: PropTypes.func.isRequired,
    children: PropTypes.oneOfType([
        PropTypes.element,
        PropTypes.arrayOf(PropTypes.element)
    ]),
    editable: PropTypes.bool,
    formName: PropTypes.string,
    updateErrors: PropTypes.func
  };

  updateFormErrors = (fieldError, fieldName) => {
    if (this.props.updateErrors) {
      this.props.updateErrors({
        [this.props.formName]: { [fieldName]: fieldError }
      });
    }
  }

  render() {
    const hydratedChildren = React.Children.map(this.props.children, (child) => {
      return React.cloneElement(child, {
        data: this.props.data,
        updateData: this.props.updateData,
        updateFormErrors: this.updateFormErrors,
        editable: this.props.editable
      });
    });

    return <div className="form">{hydratedChildren}</div>;
  }
}

