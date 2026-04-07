import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { userEvent } from '@testing-library/user-event';
import Typeahead, { TypeaheadOption } from './Typeahead';

const options: TypeaheadOption[] = [
  { id: 'opt-1', label: 'Option One', detail: 'Detailed One' },
  { id: 'opt-2', label: 'Option Two', detail: 'Detailed Two' },
  { id: 'opt-3', label: 'Option Three', detail: 'Detailed Three' },
];

const defaultProps = {
  options,
  selectedItems: [] as TypeaheadOption[],
  onSelectionChange: jest.fn(),
  placeholder: 'Search...',
};

describe('Typeahead', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders an input with placeholder text', () => {
    render(<Typeahead {...defaultProps} />);
    expect(screen.getByPlaceholderText('Search...')).toBeInTheDocument();
  });

  it('calls onInputChange when typing', async () => {
    const onInputChange = jest.fn();
    const user = userEvent.setup();
    render(<Typeahead {...defaultProps} onInputChange={onInputChange} />);
    await user.type(screen.getByPlaceholderText('Search...'), 'Opt');
    expect(onInputChange).toHaveBeenLastCalledWith('Opt');
  });

  it('displays options in the dropdown when input is not empty', async () => {
    const user = userEvent.setup();
    render(<Typeahead {...defaultProps} />);
    await user.type(screen.getByPlaceholderText('Search...'), 'foo');
    expect(screen.getByTitle('opt-1')).toBeInTheDocument();
    expect(screen.getByTitle('opt-2')).toBeInTheDocument();
    expect(screen.getByTitle('opt-3')).toBeInTheDocument();
  });

  it('hides the dropdown when the options list is empty', async () => {
    const user = userEvent.setup();
    render(<Typeahead {...defaultProps} options={[]} />);
    await user.type(screen.getByPlaceholderText('Search...'), 'foo');
    expect(screen.queryByRole('listitem')).not.toBeInTheDocument();
  });

  it('shows the detail text for options that have it', async () => {
    const user = userEvent.setup();
    render(<Typeahead {...defaultProps} />);
    await user.type(screen.getByPlaceholderText('Search...'), 'foo');
    expect(screen.getByText('Detailed One')).toBeInTheDocument();
    expect(screen.getByText('Detailed Two')).toBeInTheDocument();
  });

  it('falls back to label when an option has no detail', async () => {
    const user = userEvent.setup();
    const noDetailOptions: TypeaheadOption[] = [
      { id: 'x', label: 'Just a label' },
    ];
    render(<Typeahead {...defaultProps} options={noDetailOptions} />);
    await user.type(screen.getByPlaceholderText('Search...'), 'foo');
    expect(screen.getByText('Just a label')).toBeInTheDocument();
  });

  describe('keyboard navigation', () => {
    it('highlights the first option on ArrowDown', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} />);
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      await user.keyboard('{ArrowDown}');
      const items = screen.getAllByRole('listitem');
      expect(items[0]).toHaveClass('form__field__tags--selected');
      expect(items[1]).not.toHaveClass('form__field__tags--selected');
    });

    it('moves the highlight down on repeated ArrowDown', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} />);
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      await user.keyboard('{ArrowDown}{ArrowDown}');
      const items = screen.getAllByRole('listitem');
      expect(items[0]).not.toHaveClass('form__field__tags--selected');
      expect(items[1]).toHaveClass('form__field__tags--selected');
    });

    it('moves the highlight back up on ArrowUp', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} />);
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      await user.keyboard('{ArrowDown}{ArrowDown}{ArrowUp}');
      const items = screen.getAllByRole('listitem');
      expect(items[0]).toHaveClass('form__field__tags--selected');
      expect(items[1]).not.toHaveClass('form__field__tags--selected');
    });

    it('does not move the highlight above the first item', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} />);
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      await user.keyboard('{ArrowDown}{ArrowUp}');
      // First ArrowDown → index 0. ArrowUp from 0 → null (no selection).
      const items = screen.getAllByRole('listitem');
      expect(items[0]).not.toHaveClass('form__field__tags--selected');
    });

    it('does not move the highlight past the last item', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} />);
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      // Press down more times than there are options
      await user.keyboard('{ArrowDown}{ArrowDown}{ArrowDown}{ArrowDown}');
      const items = screen.getAllByRole('listitem');
      expect(items[items.length - 1]).toHaveClass('form__field__tags--selected');
    });

    it('selects the highlighted option on Enter and calls onSelectionChange', async () => {
      const onSelectionChange = jest.fn();
      const user = userEvent.setup();
      render(
        <Typeahead
          {...defaultProps}
          onSelectionChange={onSelectionChange}
        />
      );
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      await user.keyboard('{ArrowDown}{Enter}');
      expect(onSelectionChange).toHaveBeenCalledTimes(1);
      expect(onSelectionChange.mock.calls[0][0]).toEqual([options[0]]);
    });

    it('clears the input after selecting via keyboard', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} onSelectionChange={jest.fn()} />);
      const input = screen.getByPlaceholderText('Search...');
      await user.type(input, 'foo');
      await user.keyboard('{ArrowDown}{Enter}');
      expect(input).toHaveValue('');
    });
  });

  describe('mouse interaction', () => {
    it('calls onSelectionChange with the clicked option', async () => {
      const onSelectionChange = jest.fn();
      const user = userEvent.setup();
      render(
        <Typeahead {...defaultProps} onSelectionChange={onSelectionChange} />
      );
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      await user.click(screen.getByTitle('opt-2'));
      expect(onSelectionChange).toHaveBeenCalledTimes(1);
      expect(onSelectionChange.mock.calls[0][0]).toEqual([options[1]]);
    });

    it('clears the input after selecting via click', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} onSelectionChange={jest.fn()} />);
      const input = screen.getByPlaceholderText('Search...');
      await user.type(input, 'foo');
      await user.click(screen.getByTitle('opt-1'));
      expect(input).toHaveValue('');
    });
  });

  describe('deduplication', () => {
    it('does not show already-selected items in the dropdown', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} selectedItems={[options[0]]} />);
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      expect(screen.queryByTitle('opt-1')).not.toBeInTheDocument();
      expect(screen.getByTitle('opt-2')).toBeInTheDocument();
    });

    it('passes previously selected items through in onSelectionChange', async () => {
      const onSelectionChange = jest.fn();
      const user = userEvent.setup();
      render(
        <Typeahead
          {...defaultProps}
          selectedItems={[options[0]]}
          onSelectionChange={onSelectionChange}
        />
      );
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      await user.click(screen.getByTitle('opt-2'));
      expect(onSelectionChange.mock.calls[0][0]).toEqual([options[0], options[1]]);
    });
  });

  describe('filterLocally', () => {
    it('filters options by label when filterLocally is true', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} filterLocally />);
      await user.type(screen.getByPlaceholderText('Search...'), 'One');
      expect(screen.getByTitle('opt-1')).toBeInTheDocument();
      expect(screen.queryByTitle('opt-2')).not.toBeInTheDocument();
      expect(screen.queryByTitle('opt-3')).not.toBeInTheDocument();
    });

    it('filters options by detail when filterLocally is true', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} filterLocally />);
      await user.type(screen.getByPlaceholderText('Search...'), 'Detailed Two');
      expect(screen.queryByTitle('opt-1')).not.toBeInTheDocument();
      expect(screen.getByTitle('opt-2')).toBeInTheDocument();
    });

    it('shows no dropdown when input is empty and filterLocally is true', async () => {
      render(<Typeahead {...defaultProps} filterLocally />);
      // No typing — dropdown should not render
      expect(screen.queryByRole('listitem')).not.toBeInTheDocument();
    });
  });

  describe('allowFreeText', () => {
    it('creates a new option from the input text on Enter', async () => {
      const onSelectionChange = jest.fn();
      const user = userEvent.setup();
      render(
        <Typeahead
          {...defaultProps}
          options={[]}
          allowFreeText
          onSelectionChange={onSelectionChange}
        />
      );
      const input = screen.getByPlaceholderText('Search...');
      await user.type(input, 'my new tag');
      await user.keyboard('{Enter}');
      expect(onSelectionChange).toHaveBeenCalledWith([
        { id: 'my new tag', label: 'my new tag' },
      ]);
    });

    it('trims whitespace from the free-text option', async () => {
      const onSelectionChange = jest.fn();
      const user = userEvent.setup();
      render(
        <Typeahead
          {...defaultProps}
          options={[]}
          allowFreeText
          onSelectionChange={onSelectionChange}
        />
      );
      await user.type(screen.getByPlaceholderText('Search...'), '  spaced  ');
      await user.keyboard('{Enter}');
      expect(onSelectionChange.mock.calls[0][0]).toEqual([
        { id: 'spaced', label: 'spaced' },
      ]);
    });

    it('does not call onSelectionChange when input is whitespace-only', async () => {
      const onSelectionChange = jest.fn();
      const user = userEvent.setup();
      render(
        <Typeahead
          {...defaultProps}
          options={[]}
          allowFreeText
          onSelectionChange={onSelectionChange}
        />
      );
      await user.type(screen.getByPlaceholderText('Search...'), '   ');
      await user.keyboard('{Enter}');
      expect(onSelectionChange).not.toHaveBeenCalled();
    });

    it('prefers the highlighted dropdown option over free text on Enter', async () => {
      const onSelectionChange = jest.fn();
      const user = userEvent.setup();
      render(
        <Typeahead
          {...defaultProps}
          allowFreeText
          onSelectionChange={onSelectionChange}
        />
      );
      await user.type(screen.getByPlaceholderText('Search...'), 'foo');
      await user.keyboard('{ArrowDown}{Enter}');
      expect(onSelectionChange.mock.calls[0][0]).toEqual([options[0]]);
    });
  });

  describe('dropdown visibility', () => {
    it('hides the dropdown on blur', async () => {
      const user = userEvent.setup();
      render(<Typeahead {...defaultProps} />);
      const input = screen.getByPlaceholderText('Search...');
      await user.type(input, 'foo');
      expect(screen.getAllByRole('listitem').length).toBeGreaterThan(0);
      await user.tab();
      expect(screen.queryByRole('listitem')).not.toBeInTheDocument();
    });
  });

  describe('showSelectedItems', () => {
    it('renders selected item chips when showSelectedItems is true', () => {
      render(
        <Typeahead
          {...defaultProps}
          selectedItems={[options[0], options[1]]}
          showSelectedItems
        />
      );
      expect(screen.getByText(options[0].label)).toBeInTheDocument();
      expect(screen.getByText(options[1].label)).toBeInTheDocument();
    });

    it('does not render chips when showSelectedItems is false', () => {
      render(
        <Typeahead
          {...defaultProps}
          selectedItems={[options[0]]}
          showSelectedItems={false}
        />
      );
      expect(screen.queryByText(options[0].label)).not.toBeInTheDocument();
    });

    it('calls onSelectionChange without the removed item when a chip is clicked', async () => {
      const onSelectionChange = jest.fn();
      const user = userEvent.setup();
      render(
        <Typeahead
          {...defaultProps}
          selectedItems={[options[0], options[1]]}
          showSelectedItems
          onSelectionChange={onSelectionChange}
        />
      );
      await user.click(screen.getByText(options[0].label));
      expect(onSelectionChange).toHaveBeenCalledWith([options[1]]);
    });
  });
});
