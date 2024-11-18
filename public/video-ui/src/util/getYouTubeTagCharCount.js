export function getYouTubeTagCharCount(tags) {
  if (Array.isArray(tags)) {
    const charCount = tags.reduce((charCount, keyword) => {
      //If there is a space in the keyword, youtube adds quotation marks
      //around the keyword and counts these as spaces
      if (/\s/g.test(keyword)) {
        charCount += 2;
      }
      return (charCount += keyword.length);
    }, 0);

    //Count commas added between keywords
    if (tags.length > 0) {
      return charCount + tags.length - 1;
    }
    return charCount;
  }
  return 0;
}
