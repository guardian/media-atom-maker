import type {Image} from "./imageHelpers";

export const getGridMediaId = (image: Image) => {
  const urlParts = image.mediaId.split('/');
  return urlParts[urlParts.length - 1];
};
