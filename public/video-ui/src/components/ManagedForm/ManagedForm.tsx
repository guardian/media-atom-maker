import React from 'react';
import { ManagedField } from './ManagedField';
import { ManagedSection } from './ManagedSection';

type Props = {
  data?: any;
  updateData?: (...args: any[]) => any;
  editable?: boolean;
  formName?: string;
  formClass?: string;
  updateErrors?: (...args: any[]) => any;
  updateWarnings?: (...args: any[]) => any;
};

export class ManagedForm extends React.Component<Props> {
  static managedTypes = [ManagedField, ManagedSection];

  updateFormErrors = (fieldError: any, fieldName: any) => {
    if (this.props.updateErrors) {
      this.props.updateErrors({
        [this.props.formName]: { [fieldName]: fieldError }
      });
    }
  };

  updateWarnings = (hasFieldWarning: any, fieldName: any) => {
    if (this.props.updateWarnings) {
      this.props.updateWarnings({
        [fieldName]: hasFieldWarning
      });
    }
  };

  getFormClass = () => {
    if (
      React.Children.toArray((this.props as any).children).some(
        child => (child as any).type.componentType === 'managedSection'
      )
    ) {
      return 'atom__section__form';
    }
    return '';
  };

  render() {
    const hydratedChildren = React.Children.toArray(
      (this.props as any).children
    )
      .filter(child => !!child)
      .map(child =>
        // pass down the props to managed children only
        ManagedForm.managedTypes.indexOf((child as any).type) > -1
          ? // @ts-expect-error TS(2769): No overload matches this call.
            React.cloneElement(child, {
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
