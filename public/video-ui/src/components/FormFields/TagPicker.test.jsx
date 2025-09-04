import React from 'react';
import { render, screen } from '@testing-library/react';
import TagPicker from './TagPicker';
import '@testing-library/jest-dom';
import { userEvent } from '@testing-library/user-event';
import TagManager from '../../services/tagmanager';

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
	inputPlaceholder: 'Search for tags...',
};

describe('TagPicker', () => {
		it('searches for tags as user types', async () => {
      jest.spyOn(TagManager, 'getTagsByType').mockResolvedValue({
        data: [
          { data: { path: 'keyword/first-tag', externalName: 'Tag first external', internalName: 'Tag first (internal)' } },
          { data: { path: 'keyword/second-tag', externalName: 'Tag second external', internalName: 'Tag second (internal)' } },
        ]
      });

      const user = userEvent.setup();
      render(<TagPicker {...defaultProps} />);

      // Find the input (simulate user typing)
			const input = screen.getByPlaceholderText('Search for tags...');
      await user.type(input, 'Tag');

			// Wait for the tag search results to appear
			const result1 = await screen.findByTitle('keyword/first-tag');
			expect(result1).toBeInTheDocument();
      expect(result1).toHaveTextContent('Tag first (internal)');
			const result2 = await screen.findByTitle('keyword/second-tag');
			expect(result2).toBeInTheDocument();
      expect(result2).toHaveTextContent('Tag second (internal)');

      await user.click(result2);
      expect(defaultProps.onUpdateField.mock.calls).toHaveLength(1);
      expect(defaultProps.onUpdateField.mock.calls[0][0]).toEqual(['keyword/second-tag']);
		});

    it('handles exceptions from tag search', async () => {
      jest.spyOn(TagManager, 'getTagsByType').mockRejectedValue(new Error('Network error'));

      const user = userEvent.setup();
      render(<TagPicker {...defaultProps} />);

      // Find the input (simulate user typing)
			const input = screen.getByPlaceholderText('Search for tags...');
      await user.type(input, 'Tag');

			// Wait for the tag search results to appear
			const result1 = await screen.findByText('Tags are currently unavailable');
			expect(result1).toBeInTheDocument();
		});

    it('handles empty result set from tag search', async () => {
      jest.spyOn(TagManager, 'getTagsByType').mockResolvedValue({
        data: []
      });

      const user = userEvent.setup();
      render(<TagPicker {...defaultProps} />);

      // Find the input (simulate user typing)
			const input = screen.getByPlaceholderText('Search for tags...');
      await user.type(input, 'Tag');

			// Wait for the tag search results to appear
			// const result1 = await screen.findByText('Tags are currently unavailable');
			expect(await screen.queryByText('Tags are currently unavailable')).not.toBeInTheDocument();
		});
});
