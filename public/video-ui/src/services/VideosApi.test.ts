import VideosApi from './VideosApi';
import { apiRequest } from './apiRequest';
import { setConfig } from '../slices/config';
import { setStore } from '../util/storeAccessor';
import { setupStore } from '../util/setupStore';

jest.mock('./apiRequest', () => ({
  apiRequest: jest.fn()
}));

const mockApiRequest = jest.mocked(apiRequest);

describe('syncComposerPagePath', () => {
  beforeEach(() => {
    const store = setupStore();
    store.dispatch(setConfig({ composerUrl: 'https://composer.example' }));
    setStore(store);
    mockApiRequest.mockResolvedValue({
      proposedPath: 'world/video/test'
    } as never);
  });

  it('posts to Composer and returns the server-generated path', async () => {
    await expect(VideosApi.syncComposerPagePath('content-id')).resolves.toEqual(
      {
        proposedPath: 'world/video/test'
      }
    );

    expect(apiRequest).toHaveBeenCalledWith({
      url: 'https://composer.example/api/content/content-id/sync-path?timeZone=Europe%2FLondon',
      method: 'post',
      crossOrigin: true,
      withCredentials: true
    });
  });
});
