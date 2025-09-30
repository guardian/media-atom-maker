import { SecretsManager } from 'aws-sdk';

const secretsManagerClient = new SecretsManager();

export async function readSecretValue(secretName) {
  const secretValue = await secretsManagerClient
    .getSecretValue({
      SecretId: secretName
    })
    .promise();
  return secretValue.SecretString;
}
