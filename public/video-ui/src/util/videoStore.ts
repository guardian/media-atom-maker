type Video = {

}
class VideoStore {
  private videos: Video[] = [];

  clearVideos = () => {
    this.videos = [];
  }
  addVideos = (vids: Video[]) => {
    this.videos = [...this.videos, ...vids]
    return this.videos
  }
}

export const videoStore = new VideoStore();
