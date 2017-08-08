export function updateFormWarnings(warning) {
  return {
    type: 'FIELD_WARNINGS_UPDATE_REQUEST',
    warning: warning,
    receivedAt: Date.now()
  };
}
