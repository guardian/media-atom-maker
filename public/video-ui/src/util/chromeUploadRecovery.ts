const STORAGE_KEY = 'chrome-upload-started-at';
const HEARTBEAT_INTERVAL_MS = 1000;
export const STALE_AFTER_MS = 6000;
const EXPIRES_AFTER_MS = 5 * 60 * 1000;

let heartbeatIntervalId: number | undefined;

function isChrome(): boolean {
  return (
    /Chrome\/\d/.test(navigator.userAgent) &&
    !/Edg\/|OPR\//.test(navigator.userAgent)
  );
}

export function startChromeUploadHeartbeat(): void {
  if (!isChrome()) return;

  const writeHeartbeat = () => {
    try {
      localStorage.setItem(STORAGE_KEY, String(Date.now()));
    } catch {
      // Storage restrictions must not prevent uploading.
    }
  };

  window.clearInterval(heartbeatIntervalId);
  writeHeartbeat();
  heartbeatIntervalId = window.setInterval(
    writeHeartbeat,
    HEARTBEAT_INTERVAL_MS
  );
}

export function clearChromeUploadMarker(): void {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch {
    // Storage restrictions must not prevent uploading.
  }
}

export function stopChromeUploadHeartbeat(): void {
  // Non-uploading tabs also fire pagehide and must not clear another tab's marker.
  if (heartbeatIntervalId === undefined) return;
  window.clearInterval(heartbeatIntervalId);
  heartbeatIntervalId = undefined;
  clearChromeUploadMarker();
}

export function wasChromeUploadInterrupted(): boolean {
  if (!isChrome()) return false;

  try {
    const timestamp = localStorage.getItem(STORAGE_KEY);
    if (!timestamp) return false;

    const age = Date.now() - Number(timestamp);
    return age >= STALE_AFTER_MS && age < EXPIRES_AFTER_MS;
  } catch {
    return false;
  }
}
