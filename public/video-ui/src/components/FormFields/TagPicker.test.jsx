import React from 'react';
import { Provider } from 'react-redux';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { userEvent } from '@testing-library/user-event';
import { setupStore } from '../../util/setupStore';
import { setConfig } from '../../slices/config';

const mockedGetTagsByType = jest.fn();
jest.mock('../../services/tagmanager', () => ({
  __esModule: true,
  getTagsByType: mockedGetTagsByType
}));
import TagPicker from './TagPicker';

const defaultProps = {
	fieldName: 'Tags',
	fieldValue: [],
	placeholder: 'Add a tag',
	tagType: 'keyword',
	editable: true,
	onUpdateField: jest.fn().mockResolvedValue(),
	hasWarning: () => false,
	hasError: () => false,
	notification: { message: '' },
	inputPlaceholder: 'Search for tags...'
};

const store = setupStore();
store.dispatch(
  setConfig(Object.assign({}, {
    tagManagerUrl: 'https://tagmanager.code.dev-gutools.co.uk'
  }))
);

describe('TagPicker', () => {
		it('searches for tags as user types', async () => {
      mockedGetTagsByType.mockResolvedValue({
        data: [
          { data: { path: 'keyword/first-tag', externalName: 'Tag first external', internalName: 'Tag first (internal)' } },
          { data: { path: 'keyword/second-tag', externalName: 'Tag second external', internalName: 'Tag second (internal)' } }
        ]
      });

      const user = userEvent.setup();
      render(<Provider store={store}><TagPicker {...defaultProps} /></Provider>);

      // Find the input (simulate user typing)
			const input = screen.getByPlaceholderText('Search for tags...');
      await user.type(input, 'Tag');

			// Wait for the tag search results to appear
			const firstTagInResult = await screen.findByTitle('keyword/first-tag');
			expect(firstTagInResult).toBeInTheDocument();
      expect(firstTagInResult).toHaveTextContent('Tag first (internal)');
			const secondTagInResult = await screen.findByTitle('keyword/second-tag');
			expect(secondTagInResult).toBeInTheDocument();
      expect(secondTagInResult).toHaveTextContent('Tag second (internal)');

      await user.click(secondTagInResult);
      expect(defaultProps.onUpdateField.mock.calls).toHaveLength(1);
      expect(defaultProps.onUpdateField.mock.calls[0][0]).toEqual(['keyword/second-tag']);
		});

    it('handles exceptions from tag search', async () => {
      mockedGetTagsByType.mockRejectedValue(new Error('Network error'));

      const user = userEvent.setup();
      render(<Provider store={store}><TagPicker {...defaultProps} /></Provider>);

      // Find the input (simulate user typing)
			const input = screen.getByPlaceholderText('Search for tags...');
      await user.type(input, 'Tag');

			// Wait for the tag search results to appear
			const errorMessage = await screen.findByText('Tags are currently unavailable');
			expect(errorMessage).toBeInTheDocument();
		});

    it('handles empty result set from tag search', async () => {
      mockedGetTagsByType.mockResolvedValue({
          data: []
      });

      const user = userEvent.setup();
      render(<Provider store={store}><TagPicker {...defaultProps} /></Provider>);

      // Find the input (simulate user typing)
			const input = screen.getByPlaceholderText('Search for tags...');
      await user.type(input, 'Tag');

			// Wait for the tag search results to appear
			// const result1 = await screen.findByText('Tags are currently unavailable');
			expect(await screen.queryByText('Tags are currently unavailable')).not.toBeInTheDocument();
		});
});
