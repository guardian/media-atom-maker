import { configureStore } from '@reduxjs/toolkit';
import { createUpload, uploadParts } from '../services/UploadsApi';
import { consumeInterruptedChromeUpload } from '../util/chromeUploadRecovery';
import s3Upload, { startVideoUpload, Upload } from './s3Upload';

jest.mock('../services/UploadsApi');
jest.mock('../actions/VideoActions/getVideo', () => ({
  getVideo: () => ({ type: 'test/getVideo' })
}));
jest.mock('./uploads', () => ({
  getUploads: () => ({ type: 'test/getUploads' })
}));
jest.mock('./error', () => ({
  showError: () => ({ type: 'test/showError' })
}));

const upload: Upload = {
  id: 'upload-id',
  parts: [{ start: 0, end: 100, key: 'part-1' }],
  metadata: {
    user: 'user',
    bucket: 'bucket',
    region: 'region',
    title: 'video'
  },
  progress: {}
};

describe('Chrome upload recovery lifecycle', () => {
  beforeEach(() => {
    jest.resetAllMocks();
    localStorage.clear();
    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue('Chrome/150.0');
    jest.mocked(createUpload).mockResolvedValue(upload);
    jest.mocked(uploadParts).mockResolvedValue(true);
  });

  afterEach(() => {
    jest.restoreAllMocks();
    localStorage.clear();
  });

  it('keeps the marker throughout an upload and clears it on success', async () => {
    let completeUpload!: (value: boolean) => void;
    jest.mocked(uploadParts).mockImplementation(
      () =>
        new Promise(resolve => {
          completeUpload = resolve;
        })
    );
    const store = configureStore({ reducer: s3Upload });
    const result = store.dispatch(
      startVideoUpload({ id: 'video-id', file: new File([], 'video.mov') })
    );

    await Promise.resolve();
    await new Promise(resolve => setTimeout(resolve, 0));

    expect(localStorage.length).toBe(1);
    expect(store.getState().status).toBe('uploading');

    completeUpload(true);
    await result;

    expect(consumeInterruptedChromeUpload()).toBe(false);
    expect(store.getState().status).toBe('idle');
  });

  it.each(['creation', 'transfer'])(
    'clears the marker after a handled %s error',
    async stage => {
      if (stage === 'creation') {
        jest.mocked(createUpload).mockRejectedValue(new Error('failed'));
      } else {
        jest.mocked(uploadParts).mockRejectedValue(new Error('failed'));
      }
      const store = configureStore({ reducer: s3Upload });

      await store.dispatch(
        startVideoUpload({ id: 'video-id', file: new File([], 'video.mov') })
      );

      expect(consumeInterruptedChromeUpload()).toBe(false);
      expect(store.getState().status).toBe('error');
    }
  );
});
