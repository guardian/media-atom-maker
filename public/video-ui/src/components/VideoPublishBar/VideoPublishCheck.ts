import { Video } from "../../services/VideosApi";
import { isTaggingSupported } from "../../util/config";

export const checkVideoReadyToPublish = (video: Video): string[] => {
  if (isTaggingSupported()) {
    if (video.videoPlayerFormat === 'Default') {
      if (video.atomTagIds && video.atomTagIds.length === 0) {
        return ['Add at least one tag before publishing non-youtube video'];
      }
    }
  }
  return [];
};
