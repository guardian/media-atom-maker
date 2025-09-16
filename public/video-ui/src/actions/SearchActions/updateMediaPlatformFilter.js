export function updateMediaPlatformFilter(mediaPlatformFilter) {
  return {
    type: 'UPDATE_MEDIA_PLATFORM_FILTER',
    mediaPlatformFilter,
    receivedAt: Date.now()
  };
}
