import { Dispatch } from 'redux';
import { showWarning } from '../slices/error';

const storageKey = 'chrome-upload-started-at';
const lockName = 'chrome-upload-active';
const maxAge = 5 * 60 * 1000;
const uploadHeartbeatTimeout = 6000;
let heartbeat: number | undefined;
let releaseUploadLock: (() => void) | undefined;

function isChrome() {
  return (
    /Chrome\/\d/.test(navigator.userAgent) &&
    !/Edg\/|OPR\//.test(navigator.userAgent)
  );
}

export function markChromeUploadStarted() {
  if (!isChrome()) return;

  const updateTimestamp = () => {
    try {
      localStorage.setItem(storageKey, String(Date.now()));
    } catch {
      // Storage restrictions must not prevent uploading.
    }
  };

  window.clearInterval(heartbeat);
  updateTimestamp();
  heartbeat = window.setInterval(updateTimestamp, 1000);

  // Background tabs throttle timers to 1/min after 5m; a shared Web Lock stays held until tab exit or crash.
  releaseUploadLock?.();
  const lockReleased = new Promise<void>(resolve => {
    releaseUploadLock = resolve;
  });
  try {
    navigator.locks
      ?.request(lockName, { mode: 'shared' }, () => lockReleased)
      .catch(() => undefined);
  } catch {
    // Lock restrictions must not prevent uploading.
  }
}

export function clearChromeUploadMarker() {
  if (heartbeat === undefined) return;
  window.clearInterval(heartbeat);
  heartbeat = undefined;
  releaseUploadLock?.();
  releaseUploadLock = undefined;
  try {
    localStorage.removeItem(storageKey);
  } catch {
    // Storage restrictions must not prevent uploading.
  }
}

export function consumeInterruptedChromeUpload(): boolean {
  if (!isChrome()) return false;

  try {
    const timestamp = localStorage.getItem(storageKey);
    if (!timestamp) return false;

    const age = Date.now() - Number(timestamp);
    if (age < uploadHeartbeatTimeout) return false;

    localStorage.removeItem(storageKey);
    return age < maxAge;
  } catch {
    return false;
  }
}

export function warnAboutInterruptedChromeUpload(dispatch: Dispatch) {
  if (!isChrome()) return;

  let timestamp: string | null;
  try {
    timestamp = localStorage.getItem(storageKey);
  } catch {
    return;
  }
  if (!timestamp) return;

  const warnIfInterrupted = () => {
    if (consumeInterruptedChromeUpload()) {
      dispatch(
        showWarning(
          'Your previous upload may have been interrupted by a known Chrome issue affecting files on LucidLink. Try uploading with Firefox or copying the file to a local folder first.'
        )
      );
    }
  };

  const warn = () => {
    if (!navigator.locks?.query) {
      warnIfInterrupted();
      return;
    }

    void navigator.locks
      .query()
      .then(locks => {
        if (locks.held?.some(lock => lock.name === lockName)) return;
        warnIfInterrupted();
      })
      .catch(warnIfInterrupted);
  };

  const remaining = uploadHeartbeatTimeout - (Date.now() - Number(timestamp));
  if (remaining > 0) {
    window.setTimeout(warn, remaining);
  } else {
    warn();
  }
}
