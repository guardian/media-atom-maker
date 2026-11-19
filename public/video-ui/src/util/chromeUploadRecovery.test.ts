import {
  clearChromeUploadMarker,
  consumeInterruptedChromeUpload,
  markChromeUploadStarted
} from './chromeUploadRecovery';

describe('Chrome upload recovery', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    localStorage.clear();
    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue('Chrome/150.0');
  });

  afterEach(() => {
    clearChromeUploadMarker();
    jest.useRealTimers();
    jest.restoreAllMocks();
    localStorage.clear();
  });

  it('recognises an interrupted upload once without the previous tab session', () => {
    markChromeUploadStarted();
    sessionStorage.clear();
    jest.clearAllTimers();
    jest.advanceTimersByTime(6000);
    jest.isolateModules(() => {
      const reloaded = jest.requireActual<
        typeof import('./chromeUploadRecovery')
      >('./chromeUploadRecovery');
      expect(reloaded.consumeInterruptedChromeUpload()).toBe(true);
      expect(reloaded.consumeInterruptedChromeUpload()).toBe(false);
    });
  });

  it('keeps a live upload marker fresh without consuming it', () => {
    markChromeUploadStarted();
    jest.advanceTimersByTime(7000);
    const lastHeartbeat = Date.now();
    jest.clearAllTimers();
    jest.setSystemTime(lastHeartbeat + 5999);

    expect(consumeInterruptedChromeUpload()).toBe(false);

    jest.setSystemTime(lastHeartbeat + 6000);
    expect(consumeInterruptedChromeUpload()).toBe(true);
  });

  it('stops the heartbeat when an upload finishes', () => {
    markChromeUploadStarted();
    clearChromeUploadMarker();
    jest.advanceTimersByTime(7000);

    expect(localStorage.length).toBe(0);
    expect(consumeInterruptedChromeUpload()).toBe(false);
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

  it.each([
    'Firefox/150.0',
    'Chrome/150.0 Edg/150.0',
    'Chrome/150.0 OPR/100.0'
  ])('does not consume an interrupted upload in %s', userAgent => {
    markChromeUploadStarted();
    jest.clearAllTimers();
    jest.advanceTimersByTime(6000);
    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue(userAgent);

    expect(consumeInterruptedChromeUpload()).toBe(false);

    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue('Chrome/150.0');
    expect(consumeInterruptedChromeUpload()).toBe(true);
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
