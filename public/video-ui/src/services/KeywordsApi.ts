import { pandaReqwest } from './pandaReqwest';

export default {
  composerTagToYouTube: (tagId: string) => {
    const encodedId = encodeURIComponent(tagId);
    return pandaReqwest<string[]>({
      url: '/api/youtube/content-bundle/' + encodedId,
      method: 'get'
    });
  }
};
