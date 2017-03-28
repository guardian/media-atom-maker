export function updateFormErrors(error) {
  return {
    type:       'FORM_ERRORS_UPDATE_REQUEST',
    error:      error,
    receivedAt: Date.now()
  };
}
