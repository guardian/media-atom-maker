import React, { useState, useRef, useEffect } from 'react';

export interface TypeaheadOption {
  id: string;
  label: string;
  detail?: string;
}

interface TypeaheadProps {
  /** Options to display in the dropdown. When using async search, update this
   *  on every `onInputChange` call. When using local filtering, pass all options
   *  and set `filterLocally`. */
  options: TypeaheadOption[];
  /** Currently selected items. The component is controlled: the consumer must
   *  update this in response to `onSelectionChange`. */
  selectedItems: TypeaheadOption[];
  /** Called whenever the selection changes (item added or removed). Receives the
   *  full new selection array. */
  onSelectionChange: (items: TypeaheadOption[]) => void;
  /** Called on every keystroke with the current input text. Use this to trigger
   *  async searches. Not needed when `filterLocally` is true. */
  onInputChange?: (text: string) => void;
  placeholder?: string;
  /** When true, options are filtered client-side by matching input text against
   *  `label` and `detail`. Useful when the full option list is passed upfront. */
  filterLocally?: boolean;
  /** When true, pressing Enter with no dropdown option highlighted creates a new
   *  option from the raw input text. */
  allowFreeText?: boolean;
  /** When true (default), selected items are rendered as removable chips above
   *  the input. Set to false when the consumer renders selected items itself. */
  showSelectedItems?: boolean;
  /** HTML id for the underlying input element. */
  inputId?: string;
  className?: string;
}

const Typeahead: React.FC<TypeaheadProps> = ({
  options,
  selectedItems,
  onSelectionChange,
  onInputChange,
  placeholder,
  filterLocally = false,
  allowFreeText = false,
  showSelectedItems = true,
  inputId,
  className,
}) => {
  const [inputText, setInputText] = useState('');
  const [highlightedIndex, setHighlightedIndex] = useState<number | null>(null);
  const [showDropdown, setShowDropdown] = useState(false);
  const listRef = useRef<HTMLUListElement>(null);
  // Prevents the blur handler from hiding the dropdown when the user clicks a
  // dropdown item (mousedown fires before blur).
  const suppressBlurRef = useRef(false);

  const selectedIds = new Set(selectedItems.map(item => item.id));

  const visibleOptions = (
    filterLocally
      ? options.filter(
          opt =>
            inputText.length > 0 &&
            (opt.label.toLowerCase().includes(inputText.toLowerCase()) ||
              opt.detail?.toLowerCase().includes(inputText.toLowerCase()))
        )
      : options
  ).filter(opt => !selectedIds.has(opt.id));

  const dropdownVisible = showDropdown && visibleOptions.length > 0;

  // Scroll highlighted item into view, mirroring TagSearch scroll behaviour.
  useEffect(() => {
    if (
      highlightedIndex !== null &&
      listRef.current &&
      listRef.current.children.length > 0
    ) {
      const elementHeight = (listRef.current.children[0] as HTMLElement)
        .offsetHeight;
      listRef.current.scrollTop =
        elementHeight * (highlightedIndex === 0 ? 0 : highlightedIndex - 1);
    }
  }, [highlightedIndex]);

  const selectOption = (option: TypeaheadOption) => {
    onSelectionChange([...selectedItems, option]);
    setInputText('');
    setHighlightedIndex(null);
    setShowDropdown(false);
    onInputChange?.('');
  };

  const removeItem = (item: TypeaheadOption) => {
    onSelectionChange(selectedItems.filter(i => i.id !== item.id));
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const text = e.target.value;
    setInputText(text);
    setHighlightedIndex(null);
    setShowDropdown(true);
    onInputChange?.(text);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setShowDropdown(true);
      setHighlightedIndex(prev => {
        if (prev === null) return visibleOptions.length > 0 ? 0 : null;
        return prev < visibleOptions.length - 1 ? prev + 1 : prev;
      });
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setHighlightedIndex(prev =>
        prev === null || prev === 0 ? null : prev - 1
      );
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (highlightedIndex !== null && visibleOptions[highlightedIndex]) {
        selectOption(visibleOptions[highlightedIndex]);
      } else if (allowFreeText && inputText.trim()) {
        selectOption({ id: inputText.trim(), label: inputText.trim() });
      }
    }
  };

  const handleBlur = () => {
    if (suppressBlurRef.current) {
      suppressBlurRef.current = false;
      return;
    }
    setShowDropdown(false);
    setHighlightedIndex(null);
  };

  return (
    <div className={className}>
      {showSelectedItems && selectedItems.length > 0 && (
        <div className="typeahead__selected-items">
          {selectedItems.map(item => (
            <span
              key={item.id}
              className="form__field--multiselect__value form__field__tag__remove"
              onClick={() => removeItem(item)}
            >
              {item.label}
            </span>
          ))}
        </div>
      )}
      <input
        type="text"
        id={inputId}
        className="form__field"
        value={inputText}
        placeholder={placeholder}
        onChange={handleInputChange}
        onKeyDown={handleKeyDown}
        onBlur={handleBlur}
      />
      {dropdownVisible && (
        <ul
          ref={listRef}
          className="form__field__tags"
          onMouseDown={() => {
            suppressBlurRef.current = true;
          }}
        >
          {visibleOptions.map((option, index) => (
            <li
              key={`${option.id}-${index}`}
              className={
                'form__field__tags' +
                (index === highlightedIndex ? ' form__field__tags--selected' : '')
              }
              title={option.id}
              onClick={() => selectOption(option)}
            >
              {option.detail ?? option.label}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default Typeahead;
