//NOTE: THIS DOESN'T SAVE THE VIDEO, ONLY UPDATES THE CLIENT STATE. USE saveVideo TO SAVE
export function updateVideo(video) {
  return {
    type:       'VIDEO_UPDATE_REQUEST',
    video:      video,
    receivedAt: Date.now()
  };
}
