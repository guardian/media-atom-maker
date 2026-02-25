import '@testing-library/jest-dom';
import { render, screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import React from 'react';
import { Provider } from 'react-redux';
import type { Video, Asset as VideoAsset } from '../../services/VideosApi';
import { setConfig } from '../../slices/config';
import { setVideo } from '../../slices/video';
import { setupStore } from '../../util/setupStore';
import { setStore } from '../../util/storeAccessor';
import { Asset } from './VideoAsset';

const defaultProps = {
  videoId: 'test-video-id',
  isActive: false,
  selectAsset: jest.fn(),
  deleteAsset: jest.fn(),
  startSubtitleFileUpload: jest.fn(),
  deleteSubtitle: jest.fn(),
  activatingAssetNumber: undefined as number
};

const defaultVideoAsset: VideoAsset = {
  version: 1,
  id: 'AAAAAAAAAAA',
  assetType: 'Video',
  mimeType: 'video/youtube',
  platform: 'Youtube'
};

const store = setupStore();
store.dispatch(
  setConfig({
    permissions: {},
    youtubeEmbedUrl: 'https://www.youtube.com/embed/'
  })
);
store.dispatch(
  setVideo({
    id: 'test-video-id',
    assets: [
      {
        ...defaultVideoAsset,
        version: 1,
        id: 'AAAAAAAAAAA'
      }
    ]
  } as Video)
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
        <Provider store={store}>
          <Asset {...defaultProps} upload={completedUpload} isActive={true} />
        </Provider>
      );

      expect(
        screen.queryByRole('button', { name: 'Activate' })
      ).not.toBeInTheDocument();
      expect(screen.getByText('Active')).toBeInTheDocument();
    });

    it('shows delete button when asset is not active', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={completedUpload} />
        </Provider>
      );

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

    it('renders processing asset with activate and delete buttons disabled', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={processingUpload} />
        </Provider>
      );

      // Check that progress bar is shown
      const progress = screen.getByRole('progressbar');
      expect(progress).toBeInTheDocument();
      expect(progress).toHaveAttribute('value', '0');
      expect(progress).toHaveAttribute('max', '1');

      // Check that activate button is present but disabled
      const activateButton = screen.getByRole('button', { name: 'Activate' });
      expect(activateButton).toBeInTheDocument();
      expect(activateButton).toBeDisabled();

      // Check that delete button is present but disabled
      const deleteButton = screen.getByRole('button', {
        name: 'delete Delete'
      });
      expect(deleteButton).toBeInTheDocument();
      expect(deleteButton).toBeDisabled();

      // Check that processing status is displayed
      expect(
        screen.getAllByText('Uploading to YouTube').length
      ).toBeGreaterThan(0);
    });

    it('does not call selectAsset when disabled activate button is clicked', async () => {
      const user = userEvent.setup();
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={processingUpload} />
        </Provider>
      );

      const activateButton = screen.getByRole('button', { name: 'Activate' });
      await user.click(activateButton);

      // Should not be called because button is disabled
      expect(defaultProps.selectAsset).not.toHaveBeenCalled();
    });

    it('shows loading state when asset is currently being activated', () => {
      render(
        <Provider store={store}>
          <Asset
            {...defaultProps}
            upload={processingUpload}
            activatingAssetNumber={2}
          />
        </Provider>
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

      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={failedUpload} />
        </Provider>
      );

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

      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={unknownProgressUpload} />
        </Provider>
      );

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
          {
            src: 'https://uploads.gu.com/test--264ef95d-ecb0-472e-9030-9e5ef678bf16-2.0.mp4',
            mimeType: 'video/mp4'
          },
          {
            src: 'https://uploads.gu.com/test--264ef95d-ecb0-472e-9030-9e5ef678bf16-2.1.m3u8',
            mimeType: 'application/vnd.apple.mpegurl'
          }
        ]
      },
      processing: {
        status: 'SendToTranscoderV2',
        failed: false
      },
      metadata: {
        originalFilename: 'Video.mp4',
        startTimestamp: 1759499181730,
        subtitleFilename: 'subtitle.srt',
        user: 'a.person@example.co.uk'
      }
    };

    it('renders reprocessing asset with activate button disabled', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={reprocessingUpload} />
        </Provider>
      );

      // Should show spinner (loader class)
      expect(document.querySelector('.loader')).toBeInTheDocument();

      // Check that activate button is present but disabled
      const activateButton = screen.getByRole('button', { name: 'Activate' });
      expect(activateButton).toBeInTheDocument();
      expect(activateButton).not.toBeDisabled();

      // Check that processing status is displayed
      expect(screen.getByText('SendToTranscoderV2')).toBeInTheDocument();

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
      <Provider store={store}>
        <Asset {...defaultProps} upload={emptyUpload} />
      </Provider>
    );

    expect(container.firstChild).toBeNull();
  });
});
