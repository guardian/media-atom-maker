import {pandaReqwest} from './pandaReqwest';
import {getStore} from '../util/storeAccessor';

export function createComposerPage(id, title, data) {
  const composerUrl = getStore().getState().config.composerUrl;
  return pandaReqwest({
    url: composerUrl + '/api/content?atomPoweredVideo=true&originatingSystem=composer&type=video&initialTitle='+title,
    method: 'post',
    contentType: 'application/json',
    crossOrigin: true,
    withCredentials: true
  })
  .then(response => {
    const pageId = response.data.id;
    return pandaReqwest({
      url: composerUrl + '/api/content/' + pageId + '/preview/mainblock',
      method: 'post',
      contentType: 'application/json',
      crossOrigin: true,
      withCredentials: true,
      data: JSON.stringify(data)
    });
  });
}

