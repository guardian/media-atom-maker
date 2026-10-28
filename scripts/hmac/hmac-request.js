const crypto = require('crypto');
const url = require('url');

class HMACRequest {
  constructor ({secret}) {
    this.secret = secret;
  }

  _getToken (remoteUrl, date) {
    const urlPath = url.parse(remoteUrl).pathname;
    const content = [date, urlPath].join('\n');

    const hmac = crypto.createHmac('sha256', this.secret);
    hmac.update(content, 'utf-8');

    return `HMAC ${hmac.digest('base64')}`;
  }

  _request (remoteUrl, method, data = {}) {
    const date = new Date().toUTCString();
    const token = this._getToken(remoteUrl, date);

    const options = {
      method: method,
      headers: {
        'Content-Type': 'application/json',
        'X-Gu-Tools-HMAC-Date': date,
        'X-Gu-Tools-HMAC-Token': token,
        'X-Gu-Tools-Service-Name': 'DEV'
      }
    };
    if (Object.keys(data).length > 0) {
      options.body = JSON.stringify(data);
    }

    return fetch(remoteUrl, options).then(async response => {
      const responseBody = await response.text();
      if (!response.ok) {
        const error = new Error(`${response.status} (${response.statusText})`);
        error.status = response.status;
        error.statusText = response.statusText;
        throw error;
      }

      if (responseBody.length === 0) {
        return undefined;
      }
      if (response.headers.get('content-type')?.includes('json')) {
        return JSON.parse(responseBody);
      }
      return responseBody;
    });
  }

  get (remoteUrl) {
    return this._request(remoteUrl, 'GET');
  }

  post (remoteUrl, data) {
    return this._request(remoteUrl, 'POST', data);
  }

  put (remoteUrl, data) {
    return this._request(remoteUrl, 'PUT', data);
  }

  delete (remoteUrl) {
    return this._request(remoteUrl, 'DELETE');
  }
}

module.exports = HMACRequest;
