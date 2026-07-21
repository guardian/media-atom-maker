import React from 'react';
import PropTypes from 'prop-types';
import { ManagedField } from './ManagedField';

export class ManagedSection extends React.Component {
  static managedTypes = [ManagedField];
  static propTypes = {
    data: PropTypes.object,
    updateData: PropTypes.func,
    children: PropTypes.oneOfType([
      PropTypes.node,
      PropTypes.arrayOf(PropTypes.node)
    ]),
    editable: PropTypes.bool,
    updateErrors: PropTypes.func
  };

  static get componentType() {
    return 'managedSection';
  }

  render() {
    const hydratedChildren = React.Children.map(this.props.children, child => {
      if (child) {
        return ManagedSection.managedTypes.indexOf(child.type) > -1
          ? React.cloneElement(child, {
              data: this.props.data,
              updateData: this.props.updateData,
              updateFormErrors: this.props.updateFormErrors,
              updateWarnings: this.props.updateWarnings,
              editable: this.props.editable
            })
          : child;
      }
      return null;
    });

    return <div className="form__section">{hydratedChildren}</div>;
  }
}
