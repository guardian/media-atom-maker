export const fieldLengths = {
  title: 100, // https://developers.google.com/youtube/v3/docs/videos#snippet.title
  // Description and trail max lengths should match composer lengths
  description: {
    max: 1000,
    charMax: 2000
  },
  trail: {
    max: 1000,
    charMax: 2000
  }
};
