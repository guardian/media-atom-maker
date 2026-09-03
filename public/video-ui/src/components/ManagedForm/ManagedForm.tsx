import React from 'react';
import { ManagedField } from './ManagedField';
import { ManagedSection } from './ManagedSection';

type Props = {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  data?: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateData?: (...args: any[]) => any;
  editable?: boolean;
  formName?: string;
  formClass?: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateErrors?: (...args: any[]) => any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateWarnings?: (...args: any[]) => any;
};

export class ManagedForm extends React.Component<Props> {
  static managedTypes = [ManagedField, ManagedSection];

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateFormErrors = (fieldError: any, fieldName: any) => {
    if (this.props.updateErrors) {
      this.props.updateErrors({
        // @ts-expect-error TS(2464): A computed property name must be of type 'string',... Remove this comment to see the full error message
        [this.props.formName]: { [fieldName]: fieldError }
      });
    }
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateWarnings = (hasFieldWarning: any, fieldName: any) => {
    if (this.props.updateWarnings) {
      this.props.updateWarnings({
        [fieldName]: hasFieldWarning
      });
    }
  };

  getFormClass = () => {
    if (
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      React.Children.toArray((this.props as any).children).some(
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        child => (child as any).type.componentType === 'managedSection'
      )
    ) {
      return 'atom__section__form';
    }
    return '';
  };

  render() {
    const hydratedChildren = React.Children.toArray(
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (this.props as any).children
    )
      .filter(child => !!child)
      .map(child =>
        // pass down the props to managed children only
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
