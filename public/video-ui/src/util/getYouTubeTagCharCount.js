export function getYouTubeTagCharCount(tags) {
  if (Array.isArray(tags)) {
    return tags.reduce((charCount, keyword) => charCount += keyword.length, 0);
  }
  return 0;
}
