export function errorDetails(error) {
  if (error instanceof XMLHttpRequest) {
    let text = `${error.status}`;

    try {
      text += ` ${error.responseText}`;
    } catch (e) {
      text += ` ${error.statusText}`;
    }

    return text;
  } else {
    return `${error}`;
  }
}
