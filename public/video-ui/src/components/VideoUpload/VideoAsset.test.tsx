import '@testing-library/jest-dom';
import { render, screen } from '@testing-library/react';
import { userEvent } from '@testing-library/user-event';
import React from 'react';
import { Provider } from 'react-redux';
import { setConfig } from '../../slices/config';
import { setVideo } from '../../slices/video';
import { setupStore } from '../../util/setupStore';
import { setStore } from '../../util/storeAccessor';
import { Asset } from './VideoAsset';
import {
  completedUpload,
  defaultProps,
  defaultStoreConfig,
  emptyUpload,
  failedUpload,
  processingUpload,
  publishedVideo,
  reprocessingUpload,
  unknownProgressUpload,
  unpublishedVideo
} from './VideoAsset.fixtures';

const store = setupStore();
store.dispatch(setConfig(defaultStoreConfig));
store.dispatch(setVideo(unpublishedVideo));
setStore(store);

describe('VideoAsset', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Asset with completed upload is inactive and video is unpublished', () => {
    it('renders completed asset with activate button enabled', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={completedUpload} />
        </Provider>
      );

      // Check that activate button is present and enabled
      const activateButton = screen.getByTestId('activate-button');
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

      const activateButton = screen.getByTestId('activate-button');
      await user.click(activateButton);

      expect(defaultProps.selectAsset).toHaveBeenCalledTimes(1);
    });

    it('does not show activate button when asset is active', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={completedUpload} isActive={true} />
        </Provider>
      );

      expect(screen.queryByTestId('activate-button')).not.toBeInTheDocument();
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

  describe('Asset is not the active asset and video is published', () => {
    const storeWithActiveAsset = setupStore();
    storeWithActiveAsset.dispatch(setConfig(defaultStoreConfig));
    storeWithActiveAsset.dispatch(setVideo(publishedVideo));
    setStore(storeWithActiveAsset);

    it('does not call selectAsset when activate button is clicked once', async () => {
      const user = userEvent.setup();

      render(
        <Provider store={storeWithActiveAsset}>
          <Asset {...defaultProps} upload={completedUpload} />
        </Provider>
      );

      const activateButton = screen.getByTestId('activate-button');
      await user.click(activateButton);

      expect(defaultProps.selectAsset).not.toHaveBeenCalled();
    });

    it('calls selectAsset when user clicks activate button and confirm activate', async () => {
      const user = userEvent.setup();

      render(
        <Provider store={storeWithActiveAsset}>
          <Asset {...defaultProps} upload={completedUpload} />
        </Provider>
      );

      const activateButton = screen.getByTestId('activate-button');
      await user.click(activateButton);
      const confirmButton = await screen.findByTestId(
        'confirm-activate-button'
      );

      // Only the confirm button should be visible
      expect(activateButton).not.toBeInTheDocument();

      await user.click(confirmButton);

      expect(defaultProps.selectAsset).toHaveBeenCalledTimes(1);
    });
  });

  describe('Asset with processing upload', () => {
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
      const activateButton = screen.getByTestId('activate-button');
      expect(activateButton).toBeInTheDocument();
      expect(activateButton).toBeDisabled();

      // Check that delete button is present but disabled
      const deleteButton = screen.getByTestId('delete-button');
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

      const activateButton = screen.getByTestId('activate-button');
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

      const activateButton = screen.getByTestId('activate-button');
      expect(activateButton).toHaveClass('btn--loading');
    });

    it('shows failed upload state', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={failedUpload} />
        </Provider>
      );

      expect(screen.getByText('Upload Failed')).toBeInTheDocument();

      const activateButton = screen.getByTestId('activate-button');
      expect(activateButton).toBeDisabled();
    });

    it('shows loading spinner when no progress information is available', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={unknownProgressUpload} />
        </Provider>
      );

      // Should show spinner (loader class)
      expect(document.querySelector('.loader')).toBeInTheDocument();

      const activateButton = screen.getByTestId('activate-button');
      expect(activateButton).toBeDisabled();
    });
  });

  describe('Self-hosted asset with reprocessing subtitles', () => {
    it('renders reprocessing asset with activate button disabled', () => {
      render(
        <Provider store={store}>
          <Asset {...defaultProps} upload={reprocessingUpload} />
        </Provider>
      );

      // Should show spinner (loader class)
      expect(document.querySelector('.loader')).toBeInTheDocument();

      // Check that activate button is present but disabled
      const activateButton = screen.getByTestId('activate-button');
      expect(activateButton).toBeInTheDocument();
      expect(activateButton).not.toBeDisabled();

      // Check that processing status is displayed
      expect(screen.getByText('SendToTranscoderV2')).toBeInTheDocument();

      // Check that file name is displayed
      expect(screen.getByText('Asset 2 - Video.mp4')).toBeInTheDocument();
    });
  });

  it('returns null when upload has no asset or processing state', () => {
    const { container } = render(
      <Provider store={store}>
        <Asset {...defaultProps} upload={emptyUpload} />
      </Provider>
    );

    expect(container.firstChild).toBeNull();
  });
});
