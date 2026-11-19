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
    sessionStorage.setItem(storageKey, String(Date.now()));
  } catch {
    // Storage restrictions must not prevent uploading.
  }
}

export function clearChromeUploadMarker() {
  try {
    sessionStorage.removeItem(storageKey);
  } catch {
    // Storage restrictions must not prevent uploading.
  }
}

export function consumeInterruptedChromeUpload(): boolean {
  try {
    const timestamp = sessionStorage.getItem(storageKey);
    sessionStorage.removeItem(storageKey);
    if (!timestamp) return false;

    const age = Date.now() - Number(timestamp);
    return age >= 0 && age < maxAge;
  } catch {
    return false;
  }
}
