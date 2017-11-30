import { pandaReqwest } from './pandaReqwest';

export default {
  composerTagToYouTube: tagId => {
    const encodedId = encodeURIComponent(tagId);
    return pandaReqwest({
      url: '/api2/youtube/content-bundle/' + encodedId,
      method: 'get'
    });
  }
};
