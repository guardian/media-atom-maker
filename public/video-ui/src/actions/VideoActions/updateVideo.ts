//NOTE: THIS DOESN'T SAVE THE VIDEO, ONLY UPDATES THE CLIENT STATE. USE saveVideo TO SAVE
import { setVideo } from "../../slices/video";
import { Video } from "../../services/VideosApi";

export function updateVideo(video?: Video) {
  return setVideo(video);
}
