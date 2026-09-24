export function errorDetails(error: unknown): string {
  if (
    typeof XMLHttpRequest !== 'undefined' &&
    error instanceof XMLHttpRequest
  ) {
    let text = `${error.status}`;

    try {
      text += ` ${error.responseText}`;
    } catch (e) {
      text += ` ${error.statusText}`;
    }

    return text;
  }

  if (typeof Response !== 'undefined' && error instanceof Response) {
    return `HTTP ${error.status} ${error.statusText}`;
  }

  return error instanceof Error ? error.message : String(error);
}
