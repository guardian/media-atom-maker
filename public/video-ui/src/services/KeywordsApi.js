import { pandaReqwest } from './pandaReqwest';
import ContentApi from './capi';

export default {
  composerTagToYouTube: (tagId) => {
    const encodedId = encodeURIComponent(tagId)
    return pandaReqwest({
      url: '/api2/youtube/content-bundle/' + encodedId,
      method: 'get'
    });
  }
}
