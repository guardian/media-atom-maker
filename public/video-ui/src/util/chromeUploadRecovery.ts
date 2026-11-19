import { Dispatch } from 'redux';
import { showWarning } from '../slices/error';

const storageKey = 'chrome-upload-started-at';
const maxAge = 5 * 60 * 1000;
const uploadHeartbeatTimeout = 6000;
let heartbeat: number | undefined;

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
}

export function clearChromeUploadMarker() {
  if (heartbeat === undefined) return;
  window.clearInterval(heartbeat);
  heartbeat = undefined;
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

  const warn = () => {
    if (consumeInterruptedChromeUpload()) {
      dispatch(
        showWarning(
          'Your previous upload may have been interrupted by a known Chrome issue affecting files on LucidLink. Try uploading with Firefox or copying the file to a local folder first.'
        )
      );
    }
  };

  const remaining = uploadHeartbeatTimeout - (Date.now() - Number(timestamp));
  if (remaining > 0) {
    window.setTimeout(warn, remaining);
  } else {
    warn();
  }
}
