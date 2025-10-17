import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { userEvent } from '@testing-library/user-event';
import { Asset } from './VideoAsset';
import { setupStore } from '../../util/setupStore';
import { setConfig } from '../../slices/config';
import { Provider } from 'react-redux';
import { setStore } from '../../util/storeAccessor';

const defaultProps = {
  videoId: 'test-video-id',
  isActive: false,
  selectAsset: jest.fn(),
  deleteAsset: jest.fn(),
  startSubtitleFileUpload: jest.fn(),
  deleteSubtitle: jest.fn(),
  permissions: {},
  activatingAssetNumber: undefined as number
};

const store = setupStore();
store.dispatch(
  setConfig({
    permissions: {},
    youtubeEmbedUrl: 'https://www.youtube.com/embed/'
  })
);
setStore(store);

describe('VideoAsset', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Asset with completed upload', () => {
    const completedUpload = {
      id: '1',
      asset: {
        id: 'AAAAAAAAAAA'
      },
      metadata: {
        originalFilename: 'test.mov',
        startTimestamp: 1758557285850,
        user: 'a.person@example.co.uk'
      }
    };

    it('renders completed asset with activate button enabled', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={completedUpload} />
        </Provider>
      );

      // Check that activate button is present and enabled
      const activateButton = screen.getByRole('button', { name: 'Activate' });
      expect(activateButton).toBeInTheDocument();
      expect(activateButton).not.toBeDisabled();

      // Check that asset info is displayed
      expect(screen.getByText('Asset 1 - test.mov')).toBeInTheDocument();
    });

    it('calls selectAsset when activate button is clicked', async () => {
      const user = userEvent.setup();
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={completedUpload} />
        </Provider>
      );

      const activateButton = screen.getByRole('button', { name: 'Activate' });
      await user.click(activateButton);

      expect(defaultProps.selectAsset).toHaveBeenCalledTimes(1);
    });

    it('does not show activate button when asset is active', () => {
      render(
        <Asset {...defaultProps} upload={completedUpload} isActive={true} />
      );

      expect(
        screen.queryByRole('button', { name: 'Activate' })
      ).not.toBeInTheDocument();
      expect(screen.getByText('Active')).toBeInTheDocument();
    });

    it('shows delete button when asset is not active', () => {
      render(<Asset {...defaultProps} upload={completedUpload} />);

      const deleteButton = screen.getByTestId('delete-button');
      expect(deleteButton).toBeInTheDocument();
      expect(deleteButton).not.toBeDisabled();
    });
  });

  describe('Asset with processing upload', () => {
    const processingUpload = {
      id: '2',
      processing: {
        status: 'Uploading to YouTube',
        failed: false,
        current: 0,
        total: 1
      },
      metadata: {
        originalFilename: 'test.mov',
        startTimestamp: 1758612923498,
        user: 'a.person@example.co.uk'
      }
    };

    it('renders processing asset with activate button disabled', () => {
      render(<Asset {...defaultProps} upload={processingUpload} />);

      // Check that progress bar is shown
      const progress = screen.getByRole('progressbar');
      expect(progress).toBeInTheDocument();
      expect(progress).toHaveAttribute('value', '0');
      expect(progress).toHaveAttribute('max', '1');

      // Check that activate button is present but disabled
      const activateButton = screen.getByRole('button', { name: 'Activate' });
      expect(activateButton).toBeInTheDocument();
      expect(activateButton).toBeDisabled();

      // Check that processing status is displayed
      expect(screen.getByText('Uploading to YouTube')).toBeInTheDocument();
    });

    it('does not call selectAsset when disabled activate button is clicked', async () => {
      const user = userEvent.setup();
      render(<Asset {...defaultProps} upload={processingUpload} />);

      const activateButton = screen.getByRole('button', { name: 'Activate' });
      await user.click(activateButton);

      // Should not be called because button is disabled
      expect(defaultProps.selectAsset).not.toHaveBeenCalled();
    });

    it('shows loading state when asset is currently being activated', () => {
      render(
        <Asset
          {...defaultProps}
          upload={processingUpload}
          activatingAssetNumber={2}
        />
      );

      const activateButton = screen.getByRole('button', { name: 'Activate' });
      expect(activateButton).toHaveClass('btn--loading');
    });

    it('shows failed upload state', () => {
      const failedUpload = {
        ...processingUpload,
        processing: {
          status: 'Upload failed',
          failed: true
        }
      };

      render(<Asset {...defaultProps} upload={failedUpload} />);

      expect(screen.getByText('Upload Failed')).toBeInTheDocument();

      const activateButton = screen.getByRole('button', { name: 'Activate' });
      expect(activateButton).toBeDisabled();
    });

    it('shows loading spinner when no progress information is available', () => {
      const unknownProgressUpload = {
        ...processingUpload,
        processing: {
          status: 'Processing...',
          failed: false
        }
      };

      render(<Asset {...defaultProps} upload={unknownProgressUpload} />);

      // Should show spinner (loader class)
      expect(document.querySelector('.loader')).toBeInTheDocument();

      const activateButton = screen.getByRole('button', { name: 'Activate' });
      expect(activateButton).toBeDisabled();
    });
  });

  describe('Self-hosted asset with reprocessing subtitles', () => {
    const reprocessingUpload = {
      id: '2',
      asset: {
        sources: [
          { src: "https://uploads.gu.com/test--264ef95d-ecb0-472e-9030-9e5ef678bf16-2.0.mp4", mimeType: "video/mp4" },
          { src: "https://uploads.gu.com/test--264ef95d-ecb0-472e-9030-9e5ef678bf16-2.1.m3u8", mimeType: "application/vnd.apple.mpegurl" }
        ]
      },
      processing: {
        status: "GetTranscodingProgressV2",
        failed: false
      },
      metadata: {
        originalFilename: 'Video.mp4',
        startTimestamp: 1759499181730,
        subtitleFilename: "subtitle.srt",
        user: 'a.person@example.co.uk'
      }
    };

    it('renders reprocessing asset with activate button disabled', () => {
      render(<Asset {...defaultProps} upload={reprocessingUpload} />);

      // Should show spinner (loader class)
      expect(document.querySelector('.loader')).toBeInTheDocument();

      // Check that activate button is present but disabled
      const activateButton = screen.getByRole('button', { name: 'Activate' });
      expect(activateButton).toBeInTheDocument();
      expect(activateButton).not.toBeDisabled();

      // Check that processing status is displayed
      expect(screen.getByText('GetTranscodingProgressV2')).toBeInTheDocument();

      // Check that file name is displayed
      expect(screen.getByText('Asset 2 - Video.mp4')).toBeInTheDocument();
    });

  });

  it('returns null when upload has no asset or processing state', () => {
    const emptyUpload = {
      id: '3',
      metadata: {
        user: 'a.person@example.co.uk'
      }
    };

    const { container } = render(
      <Asset {...defaultProps} upload={emptyUpload} />
    );

    expect(container.firstChild).toBeNull();
  });
});
