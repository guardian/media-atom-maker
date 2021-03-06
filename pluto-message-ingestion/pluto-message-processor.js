const DELETE_KEY = '(DELETE)';

class PlutoMessageProcessor {
  constructor({ hostname, hmacRequest, logger }) {
    this.hostname = hostname;
    this.hmacRequest = hmacRequest;
    this.logger = logger;
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

    const messageKeys = new Set([Object.keys(message)]);

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
        this.logger.log('invalid message, props missing', {
          message: message
        });

        // `resolve` to remove message from Kinesis
        resolve('invalid message, props missing');
      }

      const project = Object.assign({}, message);

      const remoteUrl = `${this.hostname}/api/pluto/projects`;

      this.hmacRequest
        .put(remoteUrl, project)
        .then(resp => {
          this.logger.log('successfully upserted project', {
            response: resp
          });
          resolve(resp);
        })
        .catch(err => {
          const logDetail = {
            status: err.status,
            response: err.response,
            project: project
          };

          this.logger.error('failed to upsert project', logDetail);
          reject(err);
        });
    });
  }
}

module.exports = PlutoMessageProcessor;
