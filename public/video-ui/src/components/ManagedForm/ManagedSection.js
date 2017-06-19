import React from 'react';
import PropTypes from 'prop-types';

export class ManagedSection extends React.Component {
  static propTypes = {
    data: PropTypes.object,
    updateData: PropTypes.func,
    children: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.arrayOf(PropTypes.element)
    ]),
    editable: PropTypes.bool,
    updateErrors: PropTypes.func
  };

  static get componentType() {
    return 'managedSection';
  }

  render() {
    const hydratedChildren = React.Children.map(this.props.children, child => {
      return React.cloneElement(child, {
        data: this.props.data,
        updateData: this.props.updateData,
        updateFormErrors: this.props.updateFormErrors,
        editable: this.props.editable
      });
    });

    return <div className="form__section">{hydratedChildren}</div>;
  }
}
