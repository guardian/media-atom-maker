import React, {PropTypes} from 'react';

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

  updateFormErrors = (fieldErrors, fieldName) => {
    if (this.props.updateErrors) {
      this.props.updateErrors({
        [this.props.formName]: { [fieldName]: fieldErrors }
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

