import { semanticColors } from '@guardian/stand';
/**
 * @fileoverview Manage theme overrides for adopting @guardian/stand components as
 * Video Atom Maker has a dark theme and its colour tokens do not match those of @guardian/stand.
 
 * @TODO:
 * 1. Move hardcoded colours to using semantic colour tokens from @guardian/stand where possible
 * 2. Move Video Atom Maker theme to match @guardian/stand theme
 */

export const tagTableTheme = {
  row: {
    backgroundColor: '#00000000',
    borderBottom: {
      borderColor: semanticColors.border.strong
    },
    backgroundHoverColor: '#00000000',
    firstRowBackgroundColor: '#00000000',
    firstRowBackgroundHoverColor: '#00000000'
  },
  cell: {
    borderBetweenCells: {
      borderColor: semanticColors.border.strong
    }
  }
};

export const tagAutocompleteTheme = {
  input: {
    color: semanticColors.text['strong-inverse'],
    backgroundColor: '#393939',
    borderColor: semanticColors.border.strong,
    disabledBackgroundColor: '#393939'
  },
  listbox: {
    backgroundColor: '#393939',
    borderColor: semanticColors.border.strong,
    item: {
      color: semanticColors.text['strong-inverse'],
      backgroundHoverColor: '#252525',
      colorHover: semanticColors.text['strong-inverse'],
      backgroundFocusedColor: '#252525',
      colorFocused: semanticColors.text['strong-inverse']
    }
  }
};
