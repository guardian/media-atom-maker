import '@testing-library/jest-dom';
import { render, screen } from '@testing-library/react';
import React from 'react';
import { Provider } from 'react-redux';
import { setupStore } from '../../util/setupStore';
import { setStore } from '../../util/storeAccessor';
import { VideoTrail } from './VideoTrail';
import {
  defaultProps,
  s3UploadComplete,
  s3UploadError,
  s3UploadStarting,
  s3UploadUploading
} from './VideoTrail.fixtures';

const store = setupStore();
setStore(store);

describe('VideoTrail', () => {
  describe('Asset being uploaded', () => {
    it('renders a spinning loader when the upload is starting', () => {
      render(
        <Provider store={store}>
          <VideoTrail {...defaultProps} s3UploadState={s3UploadStarting} />
        </Provider>
      );

      const loader = document.querySelector('.loader');
      const progressBar = screen.queryByRole('progressbar');

      expect(loader).toBeInTheDocument();
      expect(progressBar).not.toBeInTheDocument();
    });

    it('renders upload progress bar', () => {
      render(
        <Provider store={store}>
          <VideoTrail {...defaultProps} s3UploadState={s3UploadUploading} />
        </Provider>
      );

      const progressBar = screen.queryByRole('progressbar');

      expect(progressBar).toHaveAttribute('aria-label', 'Upload progress');
      expect(progressBar).toHaveAttribute('value', '50');
      expect(progressBar).toHaveAttribute('max', '100');
    });

    it('does not render progress when upload is complete', () => {
      render(
        <Provider store={store}>
          <VideoTrail {...defaultProps} s3UploadState={s3UploadComplete} />
        </Provider>
      );

      const progressBar = screen.queryByRole('progressbar');

      expect(progressBar).not.toBeInTheDocument();
    });

    it('renders an error message when the upload fails', () => {
      render(
        <Provider store={store}>
          <VideoTrail {...defaultProps} s3UploadState={s3UploadError} />
        </Provider>
      );

      const progressBar = screen.queryByRole('progressbar');
      const errorMessage = screen.getByText(/Upload failed/i);

      expect(progressBar).not.toBeInTheDocument();
      expect(errorMessage).toBeInTheDocument();
    });
  });
});
