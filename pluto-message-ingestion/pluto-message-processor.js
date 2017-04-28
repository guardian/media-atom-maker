class PlutoMessageProcessor {
  constructor({ hostname, hmacRequest, logger }) {
    this.hostname = hostname;
    this.hmacRequest = hmacRequest;
    this.logger = logger;
  }

  process(message) {
    switch (message.type) {
      case 'project-created': {
        return this._createProject(message);
      }
      case 'project-updated': {
        return this._updateProject(message);
      }
      default: {
        return Promise.reject(`unknown message type: ${message.type}`);
      }
    }
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

  _createProject(message) {
    return new Promise((resolve, reject) => {
      if (!PlutoMessageProcessor._isValidMessage(message)) {
        this.logger.log('invalid message, props missing', {
          message: message
        });

        // `resolve` to remove message from Kinesis
        resolve('invalid message, props missing');
      }

      const project = Object.assign({}, message);

      const remoteUrl = `${this.hostname}/api2/pluto/projects`;

      this.hmacRequest
        .post(remoteUrl, project)
        .then(resp => {
          this.logger.log('successfully created project', {
            response: resp
          });
          resolve(resp);
        })
        .catch(err => {
          this.logger.error('failed to create project', {
            status: err.status,
            response: err.response,
            project: project
          });
          reject(err);
        });
    });
  }

  _updateProject(message) {
    return new Promise((resolve, reject) => {
      if (!PlutoMessageProcessor._isValidMessage(message)) {
        this.logger.log('invalid message, props missing', {
          message: message
        });

        // `resolve` to remove message from Kinesis
        resolve('invalid message, props missing');
      }

      const project = Object.assign({}, message);

      const remoteUrl = `${this.hostname}/api2/pluto/projects/${project.id}`;

      this.hmacRequest
        .put(remoteUrl, project)
        .then(resp => {
          this.logger.log('successfully updated project', {
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

          if (err.status === 404) {
            // A 404 happens when we try to `PUT` to update, but we haven't seen the project before so it doesn't exist.
            // As message processing failures are blocking and AWS Lambda will not read any new records from the
            // stream until the failed batch of records either expires or processed successfully,
            // `resolve` for the happy path so we can continue
            // http://docs.aws.amazon.com/lambda/latest/dg/retries-on-errors.html
            this.logger.error(
              'attempted to update a project that does not exist',
              logDetail
            );
            resolve('attempted to update a project that does not exist');
          } else {
            this.logger.error('failed to update project', logDetail);
            reject(err);
          }
        });
    });
  }
}

module.exports = PlutoMessageProcessor;
