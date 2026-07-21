import { Video } from '../services/VideosApi';

export const hasIconikProject = (video: Video): boolean => {
  return video.iconikData?.projectId !== undefined;
};
