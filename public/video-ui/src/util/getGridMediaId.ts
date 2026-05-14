import type { Image } from "../services/VideosApi";
import { Video } from "../services/VideosApi";
import { getAspectRatioFromVideo } from "./getAspectRatio";

export const getGridMediaId = (image: Image) => {
  const { mediaId } = image;
  if (!mediaId) {
    return undefined;
  }
  const urlParts = mediaId.split('/');
  return urlParts[urlParts.length - 1];
};

export const getGridQueryParams = (cropType: string, video: Video) => {
  if(cropType === "verticalVideo") return `cropType=${cropType}&customRatio=${cropType},9,16`;
  if(cropType === "custom") {
    const aspectRatio = getAspectRatioFromVideo(video);
    if(!aspectRatio) return `cropType=video`;
    return `cropType=custom&customRatio=custom,${aspectRatio.replaceAll(':', ",")}`;
  }
  return `cropType=${cropType}`;
};