export function getGridMediaId(image) {
  const urlParts = image.mediaId.split('/');
  return urlParts[urlParts.length - 1];
}
