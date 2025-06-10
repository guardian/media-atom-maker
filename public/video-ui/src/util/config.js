export function extractConfigFromPage() {
  const configEl = document.getElementById('config');

  if (!configEl) {
    return {};
  }

  return JSON.parse(configEl.innerHTML);
}

export function getUserTelemetryClient(stage) {
  switch (stage) {
    case 'CODE':
      return 'https://user-telemetry.code.dev-gutools.co.uk';
    case 'PROD':
      return 'https://user-telemetry.gutools.co.uk';
    default:
      return 'https://user-telemetry.local.dev-gutools.co.uk';
  }
}
