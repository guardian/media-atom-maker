import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { userEvent } from '@testing-library/user-event';
import DurationInput from './DurationInput';

const defaultProps = {
  fieldName: 'Duration',
  rawFieldValue: 125, // 2 minutes and 5 seconds
  editable: true,
  onUpdateField: jest.fn(),
  hasError: jest.fn(() => false),
  displayPlaceholder: jest.fn(() => false)
};

describe('DurationInput', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Non-editable mode', () => {
    const nonEditableProps = {
      ...defaultProps,
      editable: false
    };

    it('renders non-editable duration with formatted time display', () => {
      render(<DurationInput {...nonEditableProps} />);

      expect(screen.getByText('Duration')).toBeInTheDocument();
      expect(screen.getByText('2:05')).toBeInTheDocument();

      // Should not show input fields
      expect(screen.queryByDisplayValue('2')).not.toBeInTheDocument();
      expect(screen.queryByDisplayValue('5')).not.toBeInTheDocument();
    });

    it('renders live video display when duration is 0', () => {
      render(<DurationInput {...nonEditableProps} rawFieldValue={0} />);

      expect(screen.getByText('Live video – zero duration')).toBeInTheDocument();
      expect(screen.queryByText('0:00')).not.toBeInTheDocument();
    });

    it('shows placeholder styling when displayPlaceholder returns true', () => {
      const propsWithPlaceholder = {
        ...nonEditableProps,
        displayPlaceholder: jest.fn(() => true)
      };

      render(<DurationInput {...propsWithPlaceholder} />);

      const fieldElement = screen.getByText('2:05');
      expect(fieldElement).toHaveClass('details-list__empty');
    });
  });

  describe('Editable mode', () => {
    it('renders editable duration inputs with initial values', () => {
      render(<DurationInput {...defaultProps} />);

      // Check form elements are present
      expect(screen.getByText('Duration')).toBeInTheDocument();

      // Check input fields
      const minsInput = screen.getByDisplayValue('2');
      const secsInput = screen.getByDisplayValue('5');

      expect(minsInput).toBeInTheDocument();
      expect(secsInput).toBeInTheDocument();
      expect(minsInput).not.toBeDisabled();
      expect(secsInput).not.toBeDisabled();
    });

    it('renders live video checkbox unchecked for regular videos', () => {
      render(<DurationInput {...defaultProps} />);

      const liveCheckbox = screen.getByRole('checkbox');
      expect(liveCheckbox).toBeInTheDocument();
      expect(liveCheckbox).not.toBeChecked();
      expect(liveCheckbox).not.toBeDisabled();
      expect(screen.getByText('Live video')).toBeInTheDocument();
    });

    it('renders live video checkbox checked when duration is 0', () => {
      render(<DurationInput {...defaultProps} rawFieldValue={0} />);

      const liveCheckbox = screen.getByRole('checkbox');
      expect(liveCheckbox).toBeChecked();

      // Input fields should be hidden and disabled
      const inputsContainer = document.querySelector('.form-element--hidden');
      expect(inputsContainer).toBeInTheDocument();
    });

    it('shows error state when hasError returns true', () => {
      const errorProps = {
        ...defaultProps,
        hasError: jest.fn(() => true),
        notification: { message: 'Invalid duration' }
      };

      render(<DurationInput {...errorProps} />);

      const minsInput = screen.getByDisplayValue('2');
      const secsInput = screen.getByDisplayValue('5');

      expect(minsInput).toHaveClass('form__field--error');
      expect(secsInput).toHaveClass('form__field--error');
      expect(screen.getByText('Invalid duration')).toBeInTheDocument();
    });
  });

  describe('User interactions', () => {
    it('updates minutes input and calls onUpdateField', async () => {
      const user = userEvent.setup();
      render(<DurationInput {...defaultProps} />);

      const minsInput = screen.getByDisplayValue('2');

      await user.clear(minsInput);
      await user.type(minsInput, '3');

      // Should call onUpdateField with new total seconds (3 * 60 + 5 = 185)
      expect(defaultProps.onUpdateField).toHaveBeenCalledWith(185);
    });

    it('updates seconds input and calls onUpdateField', async () => {
      const user = userEvent.setup();
      render(<DurationInput {...defaultProps} />);

      const secsInput = screen.getByDisplayValue('5');

      await user.clear(secsInput);
      await user.type(secsInput, '30');

      // Should call onUpdateField with new total seconds (2 * 60 + 30 = 150)
      expect(defaultProps.onUpdateField).toHaveBeenCalledWith(150);
    });

    it('limits seconds input to maximum of 59', async () => {
      const user = userEvent.setup();
      render(<DurationInput {...defaultProps} />);

      const secsInput = screen.getByDisplayValue('5');

      await user.clear(secsInput);
      await user.type(secsInput, '75');

      // Should be capped at 59
      expect(secsInput).toHaveValue('59');
      expect(defaultProps.onUpdateField).toHaveBeenCalledWith(179); // 2 * 60 + 59
    });

    it('handles empty seconds input by defaulting to 0', async () => {
      const user = userEvent.setup();
      render(<DurationInput {...defaultProps} />);

      const secsInput = screen.getByDisplayValue('5');

      await user.clear(secsInput);

      expect(secsInput).toHaveValue('0');
      expect(defaultProps.onUpdateField).toHaveBeenCalledWith(120); // 2 * 60 + 0
    });

    it('toggles live video checkbox and updates duration', async () => {
      const user = userEvent.setup();
      render(<DurationInput {...defaultProps} />);

      const liveCheckbox = screen.getByRole('checkbox');

      await user.click(liveCheckbox);

      expect(defaultProps.onUpdateField).toHaveBeenCalledWith(0);
    });

    it('toggles live video checkbox off and sets duration to 1', async () => {
      const user = userEvent.setup();
      render(<DurationInput {...defaultProps} rawFieldValue={0} />);

      const liveCheckbox = screen.getByRole('checkbox');

      await user.click(liveCheckbox);

      expect(defaultProps.onUpdateField).toHaveBeenCalledWith(1);
    });

    it('disables input fields when live video is selected', () => {
      render(<DurationInput {...defaultProps} rawFieldValue={0} />);

      const allInputs = screen.getAllByDisplayValue('0');
      const minsInput = allInputs[0]; // First input with value '0'
      const secsInput = allInputs[1]; // Second input with value '0'

      expect(minsInput).toBeDisabled();
      expect(secsInput).toBeDisabled();
    });

    it('handles undefined rawFieldValue', () => {
      render(<DurationInput {...defaultProps} rawFieldValue={undefined} />);

      expect(screen.getAllByDisplayValue('0')).toHaveLength(2); // Both mins and secs inputs show '0'
    });

    it('handles zero minutes and seconds', () => {
      render(<DurationInput {...defaultProps} rawFieldValue={0} />);

      const liveCheckbox = screen.getByRole('checkbox');
      expect(liveCheckbox).toBeChecked();
    });

    it('handles large duration values', () => {
      const largeValue = 7265; // 121 minutes and 5 seconds
      render(<DurationInput {...defaultProps} rawFieldValue={largeValue} />);

      expect(screen.getByDisplayValue('121')).toBeInTheDocument();
      expect(screen.getByDisplayValue('5')).toBeInTheDocument();
    });

    it('handles non-numeric input gracefully', async () => {
      const user = userEvent.setup();
      render(<DurationInput {...defaultProps} />);

      const minsInput = screen.getByDisplayValue('2');

      await user.clear(minsInput);
      await user.type(minsInput, 'abc');

      // When parseInt('abc', 10) returns NaN, NaN * 60 + 5 = NaN
      expect(defaultProps.onUpdateField).toHaveBeenLastCalledWith(0);
    });
  });
});
