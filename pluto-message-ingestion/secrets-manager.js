const AWS = require('aws-sdk');

const EnvironmentConfig = require('./environment-config');

const secretsManagerClient = new AWS.SecretsManager();

async function readSecretValue(secretName) {
  const secretValue = await secretsManagerClient
    .getSecretValue({
      SecretId: secretName
    })
    .promise();
  return secretValue.SecretString;
}

async function readSecretConfig() {
  const hostSecretName = EnvironmentConfig.hostSecretName;
  const hmacSecretName = EnvironmentConfig.hmacSecretName;

  const host = await readSecretValue(hostSecretName);
  const hmacSecret = await readSecretValue(hmacSecretName);

  switch ([host === undefined, hmacSecret === undefined]) {
    case [true, false]:
      throw new Error(`Host secret (secret name: ${hostSecretName}) not found`);
    case [false, true]:
      throw new Error(`HMAC secret (secret name: ${hmacSecretName}) not found`);
    case [true, true]:
      throw new Error(
        `Both secrets not found: Host (secret name: ${hostSecretName}), HMAC (secret name: ${hmacSecretName})`
      );
    default:
      break;
  }

  return {
    host,
    hmacSecret
  };
}

module.exports = {
  readSecretConfig
};
