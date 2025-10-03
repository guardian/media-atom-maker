import {
  GetSecretValueCommand,
  SecretsManagerClient
} from '@aws-sdk/client-secrets-manager';
import { awsConfig } from './config';

const secretsManagerClient = new SecretsManagerClient(awsConfig);

export async function readSecretValue(
  secretName: string
): Promise<string | undefined> {
  const secretValue = await secretsManagerClient
    .send(new GetSecretValueCommand({ SecretId: secretName }))
    .then(_ => _.SecretString);
  return secretValue;
}
