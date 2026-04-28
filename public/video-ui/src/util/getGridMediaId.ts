import type { Image } from '../services/VideosApi';

export const getGridMediaId = (image: Image) => {
  const { mediaId } = image;
  if (!mediaId) {
    return undefined;
  }
  const urlParts = mediaId.split('/');
  return urlParts[urlParts.length - 1];
};
