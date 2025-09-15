export function updateShouldFilterForSelfHosted(shouldFilterForSelfHosted) {
  return {
    type: 'UPDATE_SHOULD_FILTER_FOR_SELF_HOSTED',
    shouldFilterForSelfHosted,
    receivedAt: Date.now()
  };
}
