import { reEstablishSession } from 'panda-session';
import { getStore } from '../util/storeAccessor';

const checkStatus = (res: Response) => {
  if (res.status >= 200 && res.status < 300) {
    return res;
  } else {
    throw res;
  }
};



const poll = <ResponseBodyType = any>(url: string | URL | Request, body: RequestInit, timeout: number) => {
  const endTime = Number(new Date()) + timeout;
  const interval = 100;

  const makeRequest = (resolve: (value: ResponseBodyType) => void, reject: (reason?: any) => void) => {
    fetch(url, body)
      .then(checkStatus)
      .then(response => {
        const contentTypeHeader = response.headers.get("content-type");

        if (contentTypeHeader && contentTypeHeader.includes("application/json")) {
          return response.json();
        }
        return response.text();
      })
      .then(res => {
        resolve(res);
      })
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

// https://github.com/ded/reqwest?tab=readme-ov-file#options - library that project used to use?
type ReqwestBody<RequestBodyType = any> = RequestInit & {
  url: string | URL | Request, // to do - might always be string
  headers?: Record<string, string>;
  data?: RequestBodyType;
  type?: 'html' | 'xml' | 'json' | 'jsonp' // to we 
  contentType?: string; // define enum of acceptable values?
  crossOrigin?: boolean
  withCredentials?: boolean
}

// when `timeout` > 0, the request will be retried every 100ms until success or timeout
export const pandaReqwest = <ResponseBodyType = any, RequestBodyType = any >(reqwestBody: ReqwestBody<RequestBodyType>, timeout = 0) => {
  const payload = Object.assign({ method: 'get', credentials: 'include' }, reqwestBody);

  if (payload.data) {
    payload.contentType = payload.contentType || 'application/json';

    // prettier-ignore
    if (payload.contentType === 'application/json' && typeof payload.data === 'object') {
      if (payload.headers) {
        payload.headers["Content-Type"] = "application/json";
      } else {
        payload.headers = {
          "Content-Type": "application/json"
        };
      }
      payload.body = JSON.stringify(payload.data);
    }
  }

  return poll<ResponseBodyType>(reqwestBody.url, payload, timeout);
};
