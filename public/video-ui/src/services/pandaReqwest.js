import { reEstablishSession } from 'panda-session';
import { getStore } from '../util/storeAccessor';

const checkStatus = (res) => {
  if (res.status >= 200 && res.status < 300) {
    return res;
  } else {
    throw res;
  }
}

export const poll = (url, body, timeout) => {
  const endTime = Number(new Date()) + timeout;
  const interval = 100;

  const makeRequest = (resolve, reject) => {
    fetch(url, body)
      .then(checkStatus)
      .then(response => response.json())
      .then(res => {
        resolve(res)}
      )
      .catch(err => {
        if (Number(new Date()) < endTime) {
          if (err.status === 419 || err.status == 401) {
            const store = getStore();
            const reauthUrl = store.getState().config.reauthUrl;
            reEstablishSession(reauthUrl, 5000).then(
                () => {
                  setTimeout(makeRequest, interval, resolve, reject);
                },
                error => {
                  throw error;
                });
          } else {
            setTimeout(makeRequest, interval, resolve, reject);
          }
        } else {
          reject(err);
        }
      });
  };

  return new Promise(makeRequest);
};

// when `timeout` > 0, the request will be retried every 100ms until success or timeout
export const pandaReqwest = (reqwestBody, timeout = 0) => {
  const payload = Object.assign({ method: 'get', credentials: 'include' }, reqwestBody);

  if (payload.data) {
    payload.contentType = payload.contentType || 'application/json';

    // prettier-ignore
    if (payload.contentType === 'application/json' && typeof payload.data === 'object') {
      if (payload.headers){
        payload.headers["Content-Type"] = "application/json";
      } else {
        payload.headers = {
          "Content-Type": "application/json"
        };
      }
      payload.body = JSON.stringify(payload.data);
    }
  }

  return poll(reqwestBody.url, payload, timeout);
}
