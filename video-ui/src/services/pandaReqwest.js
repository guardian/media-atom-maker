import { reEstablishSession } from 'panda-session';
import { getStore } from '../util/storeAccessor';

function checkStatus(res) {
  if (res.status >= 200 && res.status < 300) {
    return res;
  } else {
    throw res;
  }
}

export function pandaFetch(url, body) {
  return new Promise(function(resolve, reject) {
    fetch(url, body)
        .then(checkStatus)
        .then(response => response.json())
        .then(res => {
          console.log({res})
          resolve(res)}
        )
        .catch(err => {
          if (err.status == 419) {
            const store = getStore();
            var reauthUrl = store.getState().config.reauthUrl;

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
            console.log(err)
            reject(err);
          }
        });
  });
}

// when `timeout` > 0, the request will be retried every 100ms until success or timeout
export function pandaReqwest(reqwestBody, timeout = 0) {
  const payload = Object.assign({ method: 'get' }, reqwestBody);
  console.log(payload);

  if (payload.data) {
    payload.contentType = payload.contentType || 'application/json';

    // prettier-ignore
    if (payload.contentType === 'application/json' && typeof payload.data === 'object') {

      payload.data = JSON.stringify(payload.data);
    }
  }

  const promise = pandaFetch(reqwestBody.url, payload);
  console.log(promise);
  return promise;
}
