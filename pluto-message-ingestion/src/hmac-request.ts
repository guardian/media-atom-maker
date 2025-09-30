import { createHmac } from 'node:crypto';

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

async function hmacRequest({
  url,
  method,
  secret,
  data
}: {
  url: string;
  method: 'PUT' | 'DELETE';
  secret: string;
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

  console.log(`Making ${method} request to ${url}`, data);

  return fetch(url, request);
}

export async function hmacPut({
  url,
  secret,
  data
}: {
  url: string;
  secret: string;
  data: object;
}) {
  return hmacRequest({ url, method: 'PUT', secret, data });
}

export async function hmacDelete({
  url,
  secret
}: {
  url: string;
  secret: string;
}) {
  return hmacRequest({ url, method: 'DELETE', secret });
}
