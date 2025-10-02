const crypto = require('node:crypto');
const url = require('url');
const logForElk = require('./logger');

class HMACRequest {
  constructor({ serviceName, secret }) {
    this.serviceName = serviceName;
    this.secret = secret;
  }

  _getToken(remoteUrl, date) {
    const urlPath = url.parse(remoteUrl).pathname;
    const content = [date, urlPath].join('\n');
    const hmac = crypto.createHmac('sha256', this.secret);
    hmac.update(content, 'utf-8');
    return `HMAC ${hmac.digest('base64')}`;
  }

  _request(remoteUrl, method, data = {}) {
    const date = new Date().toUTCString();
    const token = this._getToken(remoteUrl, date);

    const request = {
      headers: {
        'Content-Type': 'application/json',
        'X-Gu-Tools-HMAC-Date': date,
        'X-Gu-Tools-HMAC-Token': token,
        'X-Gu-Tools-Service-Name': this.serviceName
      },
      method,
      body: Object.keys(data).length > 0 ? JSON.stringify(data) : undefined
    };

    logForElk(
      { message: `Making ${method} request to ${remoteUrl}`, data },
      'info'
    );

    return fetch(remoteUrl, request);
  }

  put(remoteUrl, data) {
    return this._request(remoteUrl, 'PUT', data);
  }

  delete(remoteUrl) {
    return this._request(remoteUrl, 'DELETE');
  }
}

module.exports = HMACRequest;
