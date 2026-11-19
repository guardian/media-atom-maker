import {
  clearChromeUploadMarker,
  consumeInterruptedChromeUpload,
  markChromeUploadStarted
} from './chromeUploadRecovery';

describe('Chrome upload recovery', () => {
  beforeEach(() => {
    sessionStorage.clear();
    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue('Chrome/150.0');
  });

  afterEach(() => {
    jest.restoreAllMocks();
    sessionStorage.clear();
  });

  it('recognises an interrupted upload once after reloading the module', () => {
    markChromeUploadStarted();
    jest.isolateModules(() => {
      const reloaded = jest.requireActual<
        typeof import('./chromeUploadRecovery')
      >('./chromeUploadRecovery');
      expect(reloaded.consumeInterruptedChromeUpload()).toBe(true);
      expect(reloaded.consumeInterruptedChromeUpload()).toBe(false);
    });
  });

  it.each([
    'Firefox/150.0',
    'Chrome/150.0 Edg/150.0',
    'Chrome/150.0 OPR/100.0'
  ])('does not track uploads in %s', userAgent => {
    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue(userAgent);
    markChromeUploadStarted();
    expect(consumeInterruptedChromeUpload()).toBe(false);
  });

  it('ignores expired markers', () => {
    jest.spyOn(Date, 'now').mockReturnValue(1000);
    markChromeUploadStarted();
    jest.spyOn(Date, 'now').mockReturnValue(1000 + 24 * 60 * 60 * 1000);
    expect(consumeInterruptedChromeUpload()).toBe(false);
  });

  it('does not fail the upload when storage is unavailable', () => {
    jest.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('storage blocked');
    });
    jest.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
      throw new Error('storage blocked');
    });
    jest.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('storage blocked');
    });

    expect(markChromeUploadStarted).not.toThrow();
    expect(clearChromeUploadMarker).not.toThrow();
    expect(consumeInterruptedChromeUpload()).toBe(false);
  });
});
