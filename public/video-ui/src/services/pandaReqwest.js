import { reEstablishSession } from 'panda-session';
import { getStore } from '../util/storeAccessor';

const checkStatus = (res) => {
  if (res.status >= 200 && res.status < 300) {
    return res;
  } else {
    throw res;
  }
}

export const pandaFetch = (url, body) => {
  return new Promise(function(resolve, reject) {
    fetch(url, body)
        .then(checkStatus)
        .then(response => response.json())
        .then(res => {
          resolve(res)}
        )
        .catch(err => {
          if (err.status == 419) {
            const store = getStore();
            const reauthUrl = store.getState().config.reauthUrl;

            reEstablishSession(reauthUrl, 5000).then(
                () => {
                  fetch(url, body)
                  .then(checkStatus)
                  .then(res => resolve(res))
                  .catch(err => reject(err));
                },
                error => {
                  throw error;
                });

          } else {
            reject(err);
          }
        });
  });
}

export const pandaReqwest = (reqwestBody) => {
  const payload = Object.assign({ method: 'get' }, reqwestBody);

  if (payload.data) {
    payload.contentType = payload.contentType || 'application/json';

    // prettier-ignore
    if (payload.contentType === 'application/json' && typeof payload.data === 'object') {

      payload.data = JSON.stringify(payload.data);
    }
  }

  return pandaFetch(reqwestBody.url, payload);
}
