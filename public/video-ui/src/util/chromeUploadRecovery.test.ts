import {
  clearChromeUploadMarker,
  STALE_AFTER_MS,
  startChromeUploadHeartbeat,
  stopChromeUploadHeartbeat,
  wasChromeUploadInterrupted
} from './chromeUploadRecovery';

describe('Chrome upload recovery', () => {
  beforeEach(() => {
    jest.useFakeTimers();
    localStorage.clear();
    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue('Chrome/150.0');
  });

  afterEach(() => {
    stopChromeUploadHeartbeat();
    jest.useRealTimers();
    jest.restoreAllMocks();
    localStorage.clear();
  });

  it('detects a stale upload marker until cleared', () => {
    startChromeUploadHeartbeat();
    jest.clearAllTimers();
    jest.advanceTimersByTime(STALE_AFTER_MS);

    expect(wasChromeUploadInterrupted()).toBe(true);
    clearChromeUploadMarker();
    expect(wasChromeUploadInterrupted()).toBe(false);
  });

  it('does not treat a live upload heartbeat as interrupted', () => {
    startChromeUploadHeartbeat();
    jest.advanceTimersByTime(STALE_AFTER_MS + 1000);

    expect(wasChromeUploadInterrupted()).toBe(false);
  });

  it('stops the heartbeat and clears the marker only for the uploading tab', () => {
    startChromeUploadHeartbeat();
    jest.isolateModules(() => {
      const otherTab = jest.requireActual<
        typeof import('./chromeUploadRecovery')
      >('./chromeUploadRecovery');
      otherTab.stopChromeUploadHeartbeat();
    });
    expect(localStorage.length).toBe(1);

    stopChromeUploadHeartbeat();
    jest.advanceTimersByTime(STALE_AFTER_MS);

    expect(localStorage.length).toBe(0);
    expect(wasChromeUploadInterrupted()).toBe(false);
  });

  it.each<[number, boolean]>([
    [STALE_AFTER_MS - 1, false],
    [STALE_AFTER_MS, true],
    [5 * 60 * 1000 - 1, true],
    [5 * 60 * 1000, false]
  ])('returns %s for a stopped marker aged %i ms', (age, expected) => {
    jest.spyOn(Date, 'now').mockReturnValue(1000);
    startChromeUploadHeartbeat();
    jest.clearAllTimers();
    jest.spyOn(Date, 'now').mockReturnValue(1000 + age);

    expect(wasChromeUploadInterrupted()).toBe(expected);
  });

  it.each([
    'Firefox/150.0',
    'Chrome/150.0 Edg/150.0',
    'Chrome/150.0 OPR/100.0'
  ])('ignores %s', userAgent => {
    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue(userAgent);
    startChromeUploadHeartbeat();
    jest.advanceTimersByTime(STALE_AFTER_MS);

    expect(localStorage.length).toBe(0);
    expect(wasChromeUploadInterrupted()).toBe(false);
  });

  it('does not throw when localStorage is unavailable', () => {
    jest.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('storage blocked');
    });
    jest.spyOn(Storage.prototype, 'removeItem').mockImplementation(() => {
      throw new Error('storage blocked');
    });
    jest.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('storage blocked');
    });

    expect(startChromeUploadHeartbeat).not.toThrow();
    expect(stopChromeUploadHeartbeat).not.toThrow();
    expect(clearChromeUploadMarker).not.toThrow();
    expect(wasChromeUploadInterrupted()).toBe(false);
  });
});
