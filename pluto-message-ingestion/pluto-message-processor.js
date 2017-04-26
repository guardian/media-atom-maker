class PlutoMessageProcessor {
  constructor({ hostname, hmacRequest, logger }) {
    this.hostname = hostname;
    this.hmacRequest = hmacRequest;
    this.logger = logger;
  }

  process(message) {
    switch (message.type) {
      case 'project-created': {
        const project = PlutoMessageProcessor._convertMessageToProject(message);

        return new Promise((resolve, reject) => {
          this._createProject(project)
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
      case 'project-updated': {
        const project = PlutoMessageProcessor._convertMessageToProject(message);

        return new Promise((resolve, reject) => {
          this._updateProject(project)
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
      default: {
        return Promise.reject(`unknown message type: ${message.type}`);
      }
    }
  }

  static _convertMessageToProject(message) {
    return {
      id: message['project_id'],
      collectionId: message['collectionId'],
      headline: message['gnm_project_headline'],
      productionOffice: message['gnm_project_production_office'],
      status: message['gnm_project_status'],
      created: message['created']
    };
  }

  _createProject(project) {
    const remoteUrl = `${this.hostname}/api2/pluto/projects`;
    return this.hmacRequest.post(remoteUrl, project);
  }

  _updateProject(project) {
    const remoteUrl = `${this.hostname}/api2/pluto/projects/${project.id}`;
    return this.hmacRequest.put(remoteUrl, project);
  }
}

module.exports = PlutoMessageProcessor;
