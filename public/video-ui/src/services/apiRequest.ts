import { reEstablishSession } from 'panda-session';
import { getStore } from '../util/storeAccessor';

type RequestConfig<RequestBodyType = unknown> = {
  url: string | URL
  headers?: Record<string, string>;
  data?: RequestBodyType;
  contentType?: string;
  crossOrigin?: boolean
  withCredentials?: boolean
  method?: string
  body?: RequestInit['body'];
}

const checkStatus = (res: Response) => {
  if (res.status >= 200 && res.status < 300) {
    return res;
  } else {
    throw res;
  }
};

const fetchWithReAuth = <ResponseBodyType>(url: string | URL, config: RequestConfig, timeout: number): Promise<ResponseBodyType> => {
  const endTime = Number(new Date()) + timeout;
  const interval = 100;

  const makeRequest = (resolve: (value: ResponseBodyType) => void, reject: (reason?: unknown) => void) => {
    fetch(url, config)
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


// when `timeout` > 0, the request will be retried every 100ms until success or timeout
export const apiRequest = <ResponseBodyType = unknown, RequestBodyType = unknown>(
  requestConfig: RequestConfig<RequestBodyType>,
  timeout = 0
): Promise<ResponseBodyType> => {
  const payload = Object.assign({ method: 'get', credentials: 'include' }, requestConfig);

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

  return fetchWithReAuth<ResponseBodyType>(requestConfig.url, payload, timeout);
};
