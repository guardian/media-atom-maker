import React from 'react';
import PropTypes from 'prop-types';
import { ManagedField } from './ManagedField';
import { ManagedSection } from './ManagedSection';

export class ManagedForm extends React.Component {
  static propTypes = {
    data: PropTypes.object,
    updateData: PropTypes.func,
    children: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.arrayOf(PropTypes.element)
    ]),
    editable: PropTypes.bool,
    formName: PropTypes.string,
    formClass: PropTypes.string,
    updateErrors: PropTypes.func,
    updateWarnings: PropTypes.func
  };

  static managedTypes = [ManagedField, ManagedSection];

  updateFormErrors = (fieldError, fieldName) => {
    if (this.props.updateErrors) {
      this.props.updateErrors({
        [this.props.formName]: { [fieldName]: fieldError }
      });
    }
  };

  updateWarnings = (hasFieldWarning, fieldName) => {
    if (this.props.updateWarnings) {
      this.props.updateWarnings({
        [fieldName]: hasFieldWarning
      });
    }
  };

  getFormClass = () => {
    if (
      React.Children
        .toArray(this.props.children)
        .some(child => child.type.componentType === 'managedSection')
    ) {
      return 'atom__section__form';
    }
    return '';
  };

  render() {
    const hydratedChildren = React.Children.map(
      this.props.children.filter(child => !!child),
      child =>
        // pass down the props to managed children only
        ManagedForm.managedTypes.indexOf(child.type) > -1
          ? React.cloneElement(child, {
              data: this.props.data,
              updateData: this.props.updateData,
              updateFormErrors: this.updateFormErrors,
              updateWarnings: this.updateWarnings,
              editable: this.props.editable
            })
          : child
    );

    return <div className={this.getFormClass()}>{hydratedChildren}</div>;
  }
}
