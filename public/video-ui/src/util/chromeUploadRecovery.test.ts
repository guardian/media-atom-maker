import { configureStore } from '@reduxjs/toolkit';
import errorReducer, {
  clearErrorAndWarning,
  showWarning
} from '../slices/error';
import {
  clearChromeUploadMarker,
  consumeInterruptedChromeUpload,
  markChromeUploadStarted,
  warnAboutInterruptedChromeUpload
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
    const dispatch = jest.fn();
    warnAboutInterruptedChromeUpload(dispatch);
    expect(dispatch).not.toHaveBeenCalled();

    jest.spyOn(navigator, 'userAgent', 'get').mockReturnValue('Chrome/150.0');
    expect(consumeInterruptedChromeUpload()).toBe(true);
  });

  it('warns immediately when the saved heartbeat is already stale', () => {
    markChromeUploadStarted();
    jest.clearAllTimers();
    jest.advanceTimersByTime(6000);
    const dispatch = jest.fn();

    warnAboutInterruptedChromeUpload(dispatch);

    expect(dispatch).toHaveBeenCalledWith(
      expect.objectContaining({ type: 'SHOW_WARNING' })
    );
  });

  it('waits only for the remaining heartbeat timeout', () => {
    markChromeUploadStarted();
    jest.clearAllTimers();
    jest.advanceTimersByTime(4000);
    const dispatch = jest.fn();

    warnAboutInterruptedChromeUpload(dispatch);
    jest.advanceTimersByTime(1999);
    expect(dispatch).not.toHaveBeenCalled();

    jest.advanceTimersByTime(1);
    expect(dispatch).toHaveBeenCalledTimes(1);
  });

  it('does not warn if another tab keeps the heartbeat fresh', () => {
    markChromeUploadStarted();
    jest.advanceTimersByTime(4000);
    const dispatch = jest.fn();

    warnAboutInterruptedChromeUpload(dispatch);
    jest.advanceTimersByTime(2000);

    expect(dispatch).not.toHaveBeenCalled();
  });

  it.each<[number, boolean]>([
    [5 * 60 * 1000 - 1, true],
    [5 * 60 * 1000, false]
  ])(
    'checks a marker aged %i ms against the five-minute limit',
    (age, expected) => {
      jest.spyOn(Date, 'now').mockReturnValue(1000);
      markChromeUploadStarted();
      jest.spyOn(Date, 'now').mockReturnValue(1000 + age);
      expect(consumeInterruptedChromeUpload()).toBe(expected);
      expect(localStorage.length).toBe(0);
    }
  );

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

  it('does not warn when a background-throttled upload tab still holds the Web Lock', async () => {
    const held: LockInfo[] = [];
    Object.defineProperty(navigator, 'locks', {
      configurable: true,
      value: {
        request: (
          name: string,
          options: LockOptions,
          callback: (lock: Lock | null) => Promise<unknown>
        ) => {
          const entry: LockInfo = { name, mode: options.mode };
          held.push(entry);
          return Promise.resolve()
            .then(() => callback({ name, mode: options.mode ?? 'exclusive' }))
            .finally(() => {
              const index = held.indexOf(entry);
              if (index >= 0) held.splice(index, 1);
            });
        },
        query: () => Promise.resolve({ held: [...held], pending: [] })
      }
    });

    try {
      markChromeUploadStarted();
      await Promise.resolve();
      // Simulate Chrome throttling background timers to 1/min so the timestamp exceeds 6s.
      jest.clearAllTimers();
      jest.advanceTimersByTime(45_000);

      const dispatch = jest.fn();
      warnAboutInterruptedChromeUpload(dispatch);
      await Promise.resolve();

      expect(dispatch).not.toHaveBeenCalled();
      expect(localStorage.length).toBe(1);

      clearChromeUploadMarker();
      await Promise.resolve();
      await Promise.resolve();
      expect(held).toHaveLength(0);

      localStorage.setItem(
        'chrome-upload-started-at',
        String(Date.now() - 10_000)
      );
      warnAboutInterruptedChromeUpload(dispatch);
      await Promise.resolve();

      expect(dispatch).toHaveBeenCalledWith(
        expect.objectContaining({ type: 'SHOW_WARNING' })
      );
    } finally {
      Reflect.deleteProperty(navigator, 'locks');
    }
  });

  it('keeps the recovery warning across the initial root IndexRedirect in app.tsx', () => {
    markChromeUploadStarted();
    jest.clearAllTimers();
    jest.advanceTimersByTime(6000);

    window.history.replaceState({}, '', '/');
    const mount = document.createElement('div');
    mount.id = 'react-mount';
    document.body.appendChild(mount);

    try {
      jest.isolateModules(() => {
        jest.doMock('../../styles/main.scss', () => ({}));
        const isolatedRouter =
          jest.requireActual<typeof import('react-router')>('react-router');
        const isolatedReact =
          jest.requireActual<typeof import('react')>('react');
        jest.doMock('../routes', () => ({
          routes: isolatedReact.createElement(
            isolatedRouter.Router,
            { history: isolatedRouter.browserHistory },
            isolatedReact.createElement(
              isolatedRouter.Route,
              { path: '/' },
              isolatedReact.createElement(isolatedRouter.IndexRedirect, {
                to: '/videos'
              }),
              isolatedReact.createElement(isolatedRouter.Route, {
                path: '/videos',
                component: () => null
              })
            )
          )
        }));

        jest.requireActual('../app');
        const { getStore } =
          jest.requireActual<typeof import('./storeAccessor')>(
            './storeAccessor'
          );

        expect(window.location.pathname).toBe('/videos');
        expect(getStore().getState().error.warnings).toEqual([
          expect.objectContaining({
            message: expect.stringContaining('LucidLink')
          })
        ]);
      });
    } finally {
      mount.remove();
    }
  });

  it('preserves the recovery warning alongside other warnings until cleared', () => {
    const store = configureStore({ reducer: { error: errorReducer } });
    markChromeUploadStarted();
    jest.clearAllTimers();
    jest.advanceTimersByTime(6000);

    warnAboutInterruptedChromeUpload(store.dispatch);
    store.dispatch(
      showWarning('Click "Edit Assets" to add missing Iconik data')
    );
    store.dispatch(
      showWarning('Click "Edit Assets" to add missing Iconik data')
    );

    expect(store.getState().error.warnings.map(w => w.message)).toEqual([
      expect.stringContaining('LucidLink'),
      'Click "Edit Assets" to add missing Iconik data'
    ]);

    store.dispatch(clearErrorAndWarning());
    expect(store.getState().error.warnings).toEqual([]);
  });
});
