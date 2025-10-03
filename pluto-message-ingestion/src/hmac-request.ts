import { createHmac } from 'node:crypto';
import type { Logger } from './logging';

function getToken({
  url,
  date,
  secret
}: {
  url: string;
  date: string;
  secret: string;
}) {
  const { pathname } = new URL(url);
  const content = [date, pathname].join('\n');
  const hmac = createHmac('sha256', secret);
  hmac.update(content, 'utf-8');
  return `HMAC ${hmac.digest('base64')}`;
}

export function createHmacClient(logger: Logger, secret: string) {
  async function hmacRequest({
    url,
    method,
    data
  }: {
    url: string;
    method: 'PUT' | 'DELETE';
    data?: object;
  }) {
    const date = new Date().toUTCString();
    const token = getToken({ url, date, secret });

    const request = {
      headers: {
        'Content-Type': 'application/json',
        'X-Gu-Tools-HMAC-Date': date,
        'X-Gu-Tools-HMAC-Token': token,
        'X-Gu-Tools-Service-Name': 'pluto-message-ingestion'
      },
      method,
      body: data ? JSON.stringify(data) : undefined
    };

    logger.log({ message: `Making ${method} request to ${url}`, data });

    return fetch(url, request);
  }

  async function hmacPut({ url, data }: { url: string; data: object }) {
    return hmacRequest({ url, method: 'PUT', data });
  }

  async function hmacDelete({ url }: { url: string }) {
    return hmacRequest({ url, method: 'DELETE' });
  }

  return { hmacPut, hmacDelete };
}
