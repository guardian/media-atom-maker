class EnvironmentConfig {
  static get stack() {
    return process.env.STACK || 'media-service';
  }

  static get stage() {
    return process.env.STAGE || 'DEV';
  }

  static get app() {
    return process.env.APP || 'pluto-message-ingestion';
  }

  static get isDev() {
    return this.stage === 'DEV';
  }

  static get bucket() {
    return process.env.CONFIG_BUCKET;
  }

  static get region() {
    return process.env.REGION || 'eu-west-1';
  }

  static get profile() {
    return process.env.PROFILE || 'media-service';
  }
}

module.exports = EnvironmentConfig;
