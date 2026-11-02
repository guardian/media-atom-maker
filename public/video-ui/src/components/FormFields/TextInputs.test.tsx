import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import FieldNotification from '../../constants/FieldNotification';
import TextInput from './TextInput';
import TextAreaInput from './TextAreaInput';

type Props = React.ComponentProps<typeof TextInput> &
  React.ComponentProps<typeof TextAreaInput>;

const textInputs: {
  name: string;
  Component: React.ComponentType<Props>;
}[] = [
  { name: 'TextInput', Component: props => <TextInput {...props} /> },
  { name: 'TextAreaInput', Component: props => <TextAreaInput {...props} /> }
];

describe.each(textInputs)('$name', ({ Component }) => {
  const props: React.ComponentProps<typeof Component> = {
    fieldValue: 'Title',
    editable: true,
    fieldName: 'Title',
    notification: null,
    placeholder: 'No Title',
    displayPlaceholder: (placeholder, value) => placeholder === value,
    hasError: ({ notification }) =>
      notification && notification.type === FieldNotification.error,
    onUpdateField: jest.fn()
  };

  it('keeps an omitted length unlimited and passes string updates', () => {
    const onUpdateField = jest.fn();
    render(
      <Component
        {...props}
        input={{ 'aria-label': 'Title' }}
        onUpdateField={onUpdateField}
      />
    );

    const input = screen.getByRole('textbox', { name: 'Title' });
    expect(input).not.toHaveAttribute('maxlength');
    expect(input).toHaveValue('Title');
    if (input instanceof HTMLTextAreaElement) {
      expect(input.rows).toBe(5);
    }

    fireEvent.change(input, { target: { value: 'New title' } });
    expect(onUpdateField).toHaveBeenCalledWith('New title');
    expect(screen.queryByText('Invalid title')).not.toBeInTheDocument();
  });

  it('applies numeric limits and renders validation errors', () => {
    render(
      <Component
        {...props}
        maxLength={100}
        rows={8}
        notification={
          new FieldNotification(
            'required',
            'Invalid title',
            FieldNotification.error
          )
        }
      />
    );

    const input = screen.getByRole('textbox');
    expect(input).toHaveAttribute('maxlength', '100');
    expect(screen.getByText('Invalid title')).toBeInTheDocument();
    if (input instanceof HTMLTextAreaElement) {
      expect(input.rows).toBe(8);
    }
  });

  it('shows the read-only placeholder without an input', () => {
    render(
      <Component {...props} editable={false} fieldValue={props.placeholder} />
    );

    expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
    expect(screen.getByText('No Title')).toHaveClass('details-list__empty');
  });
});
