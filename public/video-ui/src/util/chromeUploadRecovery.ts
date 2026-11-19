const storageKey = 'chrome-upload-started-at';
const maxAge = 24 * 60 * 60 * 1000;
export const uploadHeartbeatTimeout = 6000;
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
