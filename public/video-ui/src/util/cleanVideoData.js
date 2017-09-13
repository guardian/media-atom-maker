export function cleanVideoData(data) {
  const cleanedData = Object.assign({}, data);

  Object.keys(cleanedData).forEach(key => {
    if (cleanedData[key] === '') {
      cleanedData[key] = null;
    }
  });

  ['posterImage', 'trailImage'].forEach(image => {
    if (
      cleanedData[image] &&
      cleanedData[image].assets &&
      cleanedData[image].assets.length === 0
    ) {
      delete cleanedData[image];
    }
  });

  return cleanedData;
}
