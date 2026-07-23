import { Video } from '../services/VideosApi';

export const hasIconikOrPlutoProject = (video: Video): boolean => {
  return (
    Boolean(video.iconikData?.projectId) || Boolean(video.plutoData?.projectId)
  );
};
