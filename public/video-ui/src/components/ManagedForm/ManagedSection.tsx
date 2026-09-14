import React from 'react';
import { ManagedField } from './ManagedField';

type Props = {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  data?: any;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateData?: (...args: any[]) => any;
  editable?: boolean;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  updateErrors?: (...args: any[]) => any;
};

export class ManagedSection extends React.Component<Props> {
  static managedTypes = [ManagedField];

  static get componentType() {
    return 'managedSection';
  }

  render() {
    const hydratedChildren = React.Children.map(
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      (this.props as any).children,
      child => {
        if (child) {
          return ManagedSection.managedTypes.indexOf(child.type) > -1
            ? React.cloneElement(child, {
                data: this.props.data,
                updateData: this.props.updateData,
                // @ts-expect-error TS(2551): Property 'updateFormErrors' does not exist on type... Remove this comment to see the full error message
                updateFormErrors: this.props.updateFormErrors,
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                updateWarnings: (this.props as any).updateWarnings,
                editable: this.props.editable
              })
            : child;
        }
        return null;
      }
    );

    return <div className="form__section">{hydratedChildren}</div>;
  }
}
