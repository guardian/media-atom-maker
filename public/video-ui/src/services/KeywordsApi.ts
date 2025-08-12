import { apiRequest } from './apiRequest';

export default {
  composerTagToYouTube: (tagId: string) => {
    const encodedId = encodeURIComponent(tagId);
    return apiRequest<string>({
      url: '/api/youtube/content-bundle/' + encodedId,
      method: 'get'
    });
  }
};
