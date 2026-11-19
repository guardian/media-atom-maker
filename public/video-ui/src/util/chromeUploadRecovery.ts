const storageKey = 'chrome-upload-started-at';
const maxAge = 24 * 60 * 60 * 1000;

export function markChromeUploadStarted() {
  if (
    !/Chrome\/\d/.test(navigator.userAgent) ||
    /Edg\/|OPR\//.test(navigator.userAgent)
  ) {
    return;
  }

  try {
    localStorage.setItem(storageKey, String(Date.now()));
  } catch {
    // Storage restrictions must not prevent uploading.
  }
}

export function clearChromeUploadMarker() {
  try {
    localStorage.removeItem(storageKey);
  } catch {
    // Storage restrictions must not prevent uploading.
  }
}

export function consumeInterruptedChromeUpload(): boolean {
  try {
    const timestamp = localStorage.getItem(storageKey);
    localStorage.removeItem(storageKey);
    if (!timestamp) return false;

    const age = Date.now() - Number(timestamp);
    return age >= 0 && age < maxAge;
  } catch {
    return false;
  }
}
