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
        this.logger.log('Project', project);

        return new Promise((resolve, reject) => {
          this._createProject(project)
            .then(resp => {
              this.logger.log('successfully added project', { response: resp });
              resolve(resp);
            })
            .catch(err => {
              this.logger.error('failed to add project', {
                status: err.status,
                response: err.response,
                project: project
              });
              reject(err.response);
            });
        });
      }
      case 'project-updated': {
        const project = PlutoMessageProcessor._convertMessageToProject(message);
        this.logger.log('Project', project);

        return new Promise((resolve, reject) => {
          this._updateProject(project)
            .then(resp => {
              this.logger.log('successfully updated project', {
                response: resp
              });
              resolve(resp);
            })
            .catch(err => {
              this.logger.error('failed to update project', {
                status: err.status,
                response: err.response,
                project: project
              });
              reject(err.response);
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
    this.logger.log(`making a request to ${remoteUrl}`, project);
    return this.hmacRequest.post(remoteUrl, project);
  }

  _updateProject(project) {
    const remoteUrl = `${this.hostname}/api2/pluto/projects/${project.id}`;
    this.logger.log(`making a request to ${remoteUrl}`, project);
    return this.hmacRequest.put(remoteUrl, project);
  }
}

module.exports = PlutoMessageProcessor;
