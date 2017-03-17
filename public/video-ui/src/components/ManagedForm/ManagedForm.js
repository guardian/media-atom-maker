import React, {PropTypes} from 'react';

export class ManagedForm extends React.Component {

  static propTypes =  {
    data: PropTypes.object,
    updateData: PropTypes.func.isRequired,
    children: PropTypes.oneOfType([
        PropTypes.element,
        PropTypes.arrayOf(PropTypes.element)
    ]),
    editable: PropTypes.bool
  };

  state: {
    formErrors: [],
    formWarnings: []
  }

  updateFormErrors = (fieldErrors, fieldName) => {
    this.setState({
      formErrors: {
        [fieldName]: fieldErrors
      }
    });
  }

  updateFormWarnings = (fieldWarnings, fieldName) => {
    this.setState({
      formWarning: {
        [fieldName]: fieldWarning
      }
    });
  }

  render() {
    const hydratedChildren = React.Children.map(this.props.children, (child) => {
      return React.cloneElement(child, {
        data: this.props.data,
        updateData: this.props.updateData,
        updateFormErrors: this.updateFormErrors,
        updateFormWarnings: this.updateFormWarnings,
        editable: this.props.editable
      });
    });

    return <div className="form">{hydratedChildren}</div>;
  }
}

