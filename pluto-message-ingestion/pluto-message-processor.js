const logForElk = require('./logger');

const DELETE_KEY = '(DELETE)';

class PlutoMessageProcessor {
  constructor({ hostname, hmacRequest }) {
    this.hostname = hostname;
    this.hmacRequest = hmacRequest;
  }

  process(message) {
    const upsertMessageTypes = ['project-created', 'project-updated'];

    if (upsertMessageTypes.includes(message.type)) {
      if (message.commissionTitle === DELETE_KEY) {
        return this._deleteProject(message);
      }

      return this._upsertProject(message);
    }

    return Promise.reject(`unknown message type: ${message.type}`);
  }

  static _isValidMessage(message) {
    const requiredKeys = new Set([
      'id',
      'title',
      'status',
      'commissionId',
      'commissionTitle',
      'productionOffice',
      'created'
    ]);

    const messageKeys = new Set(Object.keys(message));

    const diff = new Set(
      [...requiredKeys].filter(key => !messageKeys.has(key))
    );

    return diff.size === 0;
  }

  _deleteProject({ commissionId }) {
    const remoteUrl = `${this.hostname}/api/pluto/commissions/${commissionId}`;
    return this.hmacRequest.delete(remoteUrl);
  }

  _upsertProject(message) {
    return new Promise((resolve, reject) => {
      if (!PlutoMessageProcessor._isValidMessage(message)) {
        logForElk(
          {
            message: 'invalid message, props missing',
            data: {
              message
            }
          },
          'error'
        );

        // `resolve` to remove message from Kinesis
        return resolve('invalid message, props missing');
      }

      const project = Object.assign({}, message);

      const remoteUrl = `${this.hostname}/api/pluto/projects`;

      this.hmacRequest
        .put(remoteUrl, project)
        .then(resp => {
          if (resp.ok) {
            logForElk(
              {
                message: 'successfully upserted project',
                response: resp.body,
                project
              },
              'log'
            );
            resolve(resp);
          } else {
            logForElk({
              message: 'failed to upsert project',
              extraDetail: {
                status: resp.status,
                response: resp.body,
                project
              }
            });
            reject(resp);
          }
        })
        .catch(err => {
          logForElk(
            {
              message: 'failed to upsert project - request failed',
              extraDetail: {
                project,
                stack_trace: err.stack || undefined,
                error: err.toString ? err.toString() : err
              }
            },
            'error'
          );
          reject(err);
        });
    });
  }
}

module.exports = PlutoMessageProcessor;
