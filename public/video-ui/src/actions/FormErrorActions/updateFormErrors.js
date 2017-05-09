export function updateFormErrors(error) {
  return {
    type: 'CHECKED_FIELDS_UPDATE_REQUEST',
    error: error,
    receivedAt: Date.now()
  };
}
